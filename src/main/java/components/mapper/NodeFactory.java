package components.mapper;

import components.Project;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.json.JSONObject;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.MappingRulesUtils;
import utils.NodeUtils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class NodeFactory {
    private final static Logger logger = LoggerFactory.getLogger(NodeFactory.class);

    private static final JSONObject mappingRules;

    static {
        try {
            mappingRules = MappingRulesUtils.read(Project.conf.getMapper().getRules().getCitygml(),
                    Project.conf.getMapper().getRules().getGml(),
                    Project.conf.getMapper().getRules().getXal(),
                    Project.conf.getMapper().getRules().getGeneric(),
                    Project.conf.getMapper().getRules().getPrintable());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // This must be called first BEFORE any function can run from this class
    // If init has already been executed, subsequent calls will do nothing
    public static void createRootNodes(GraphDatabaseService graphDb) {
        try (Transaction tx = graphDb.beginTx()) {
            // Create root nodes
            tx.createNode(LabelFactory.ROOT_MAPPER);
            tx.createNode(LabelFactory.ROOT_MATCHER);
            logger.debug("Created root nodes {} and {}", LabelFactory.ROOT_MAPPER, LabelFactory.ROOT_MATCHER);
            tx.commit();
        }
    }

    public static Node getMapperRootNode(Transaction tx) {
        return NodeUtils.findFirst(tx, LabelFactory.ROOT_MAPPER);
    }

    public static Node getMatcherRootNode(Transaction tx) {
        return NodeUtils.findFirst(tx, LabelFactory.ROOT_MATCHER);
    }

    public static Node create(GraphDatabaseService graphDb, Object object, boolean isOld) {
        Node result = null;
        try (Transaction tx = graphDb.beginTx()) {
            ArrayList<Object> mapped = new ArrayList<>(); // TODO Make sure this is emptied after each commit
            result = create(tx, object, isOld, mapped);
            tx.commit(); // Commit once per (e.g. top-level) object
        }
        return result;
    }

    // Recursive auxiliary function
    // TODO Currently only one commit per object -> Allow batch commit?
    private static Node create(Transaction tx, Object object, boolean isOld, ArrayList<Object> mapped) {
        if (object == null) {
            return null;
        }

        Node result = null;
        // Get the object's class
        Class objectClass = object.getClass();

        // Get class hierarchy
        ArrayList<Class> objectClasses = new ArrayList<>();
        Class tmpObjectClass = objectClass;
        while (tmpObjectClass != null && !tmpObjectClass.equals(Object.class)) {
            objectClasses.add(tmpObjectClass);
            tmpObjectClass = tmpObjectClass.getSuperclass();
        }

        // Create a node with labels named after its class hierarchy
        if (object instanceof AbstractFeature) {
            logger.debug("Create node {}", objectClass.getSimpleName());
        }
        result = tx.createNode();
        for (Class cl : objectClasses) {
            result.addLabel(Label.label(getClassName(cl)));
        }

        // Get all properties and methods inherited except from Object class
        try {
            for (Class cl : objectClasses) {
                for (PropertyDescriptor propertyDescriptor
                        : Introspector.getBeanInfo(cl, cl.getSuperclass()).getPropertyDescriptors()) {
                    Method getter = propertyDescriptor.getReadMethod();
                    if (getter == null) {
                        // The current property does not have an accessible getter
                        break;
                    }

                    Object childObject = null;
                    try {
                        childObject = getter.invoke(object);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        // Private or inaccessible methods -> no mapping
                        continue;
                    }
                    if (childObject == null) {
                        continue;
                    }

                    // Mapped objects are prioritized based on their class hierarchy
                    // Objects "nearer" to the lowest class shall be prioritized over those of superclasses
                    // This way, endless loops can be avoided (i.e. same objects are mapped over and over again)
                    if (mapped.contains(childObject)) {
                        continue;
                    } else {
                        mapped.add(childObject);
                    }

                    // Check if the selected attribute of selected class should be mapped
                    if (mappingRules.has(getClassName(cl))
                            && ((JSONObject) mappingRules.get(getClassName(cl))).has(getter.getName())) {
                        if (isPrintable(getter.getReturnType())) {
                            String propertyName = getter.getName();
                            String propertyValue = childObject.toString();
                            result.setProperty(propertyName, propertyValue);
                            IndexFactory.setHrefId(tx, propertyName, propertyValue, isOld, result);
                        } else if (getter.getReturnType().isArray()) {
                            if (childObject instanceof Object[]) {
                                Object[] values = (Object[]) childObject;
                                int count = 0;
                                for (Object v : values) {
                                    // Recursively map sub-elements
                                    Node vNode = create(tx, v, isOld, mapped);
                                    Relationship rel = result.createRelationshipTo(vNode,
                                            RelationshipType.withName(getter.getName()));
                                    // Additional metadata
                                    rel.setProperty(PropertyFactory.AGGREGATION_TYPE.toString(),
                                            PropertyFactory.TYPE_ARRAY.toString());
                                    rel.setProperty(PropertyFactory.INDEX.toString(), count++);
                                }
                            }
                        } else if (Collection.class.isAssignableFrom(getter.getReturnType())) {
                            if (childObject instanceof Collection<?>) {
                                Collection<?> values = (Collection<?>) childObject;
                                int count = 0;
                                for (Object v : values) {
                                    // Recursively map sub-elements
                                    Node vNode = create(tx, v, isOld, mapped);
                                    Relationship rel = result.createRelationshipTo(vNode,
                                            RelationshipType.withName(getter.getName()));
                                    // Additional metadata
                                    rel.setProperty(PropertyFactory.AGGREGATION_TYPE.toString(),
                                            PropertyFactory.TYPE_COLLECTION.toString());
                                    rel.setProperty(PropertyFactory.INDEX.toString(), count++);
                                }
                            }
                        } else if (Map.class.isAssignableFrom(getter.getReturnType())) {
                            if (childObject instanceof Map<?, ?>) {
                                // Fill in the node with map entries
                                for (Map.Entry<?, ?> entry : ((Map<?, ?>) childObject).entrySet()) {
                                    // TODO Store all entries in one single node if they are of primitive type?
                                    // Recursively map sub-elements
                                    Node entryNode = create(tx, entry.getValue(), isOld, mapped);
                                    Relationship rel = result.createRelationshipTo(entryNode,
                                            RelationshipType.withName(getter.getName()));
                                    // Additional metadata
                                    rel.setProperty(PropertyFactory.AGGREGATION_TYPE.toString(),
                                            PropertyFactory.TYPE_MAP);
                                    rel.setProperty(PropertyFactory.MAP_KEY.toString(), entry.getKey());
                                }
                            }
                        } else {
                            // Is a complex type
                            Node childNode = create(tx, childObject, isOld, mapped);
                            Relationship rel
                                    = result.createRelationshipTo(childNode, RelationshipType.withName(getter.getName()));
                        }
                    }
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private static String getClassName(Class cl) {
        return Project.conf.getMapper().getFullName() ? cl.getName() : cl.getSimpleName();
    }

    private static boolean isPrintable(Class cl) throws NoSuchMethodException {
        return cl.isPrimitive()
                || cl.equals(String.class)
                || cl.equals(ZonedDateTime.class);
    }

    /*
    // While mapping in chunks, top-level features cannot have their bounding shapes calculated
    // --> Call this function AFTER mapping is complete
    public void calcBboxAll() {
        logger.info("Calculating BBOX of all top-level features ---");

        try (Transaction tx = graphDb.beginTx()) {
            try (ResourceIterator<Node> buildingNodes = tx.findNodes(Label.label(Building.class.getName()))) {
                while (buildingNodes.hasNext()) {
                    Node buildingNode = buildingNodes.next();

                }
                tx.commit();
            }

            // get buildings from the old/new city model
            Node cityModel = GraphUtil.findFirstChildOfNode(mapperRootNode, isOld ? EnumClasses.GMLRelTypes.OLD_CITY_MODEL : EnumClasses.GMLRelTypes.NEW_CITY_MODEL);
            for (Node buildingNode : GraphUtil.findBuildings(cityModel)) {

                countTrans++;

                if (countTrans % SETTINGS.BATCH_SIZE_TRANSACTIONS == 0) {
                    // logger.info("Processed buildings: " + countTrans);
                    mapperTx.success();
                    mapperTx.close();
                    mapperTx = graphDb.beginTx();
                }

                attachBbox(buildingNode, new Building());
            }

            logger.info("--- Calculated BBOX of all top-level features");
        }
    }

    private void attachBbox(Node buildingNode, AbstractBuilding building) {
        if (GraphUtil.findFirstChildOfNode(buildingNode, EnumClasses.GMLRelTypes.BOUNDED_BY) != null) {
            buildingNode.setProperty(InternalMappingProperties.BOUNDING_SHAPE_CREATED.toString(), "false");

            // BuildingPart
            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.BUILDING_PART)) {
                for (Node n : GraphUtil.findChildrenOfNode(buildingNode, EnumClasses.GMLRelTypes.BUILDING_PART)) {
                    // also attach bounding shapes to building parts
                    attachBbox(GraphUtil.findFirstChildOfNode(n, EnumClasses.GMLRelTypes.OBJECT), new BuildingPart());
                }
            }
        } else {
            buildingNode.setProperty(InternalMappingProperties.BOUNDING_SHAPE_CREATED.toString(), "true");

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "... calculating bounding shape of "
                        + buildingNode.getLabels().iterator().next().toString() + " "
                        + buildingNode.getProperty("id").toString() + " ...");
            }

            // LoD0
            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.LOD0_FOOT_PRINT)) {
                MultiSurfaceProperty lod0FootPrint = BoundingBoxCalculator.createMultiSurfaceProperty(
                        GraphUtil.findFirstChildOfNode(buildingNode, EnumClasses.GMLRelTypes.LOD0_FOOT_PRINT));
                building.setLod0FootPrint(lod0FootPrint);
            }

            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.LOD0_ROOF_EDGE)) {
                MultiSurfaceProperty lod0RoofEdge = BoundingBoxCalculator.createMultiSurfaceProperty(
                        GraphUtil.findFirstChildOfNode(buildingNode, EnumClasses.GMLRelTypes.LOD0_ROOF_EDGE));
                building.setLod0RoofEdge(lod0RoofEdge);
            }

            // LoD1-4 Solid
            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.LOD1_SOLID)) {
                SolidProperty lod1Solid = BoundingBoxCalculator.createSolidProperty(
                        GraphUtil.findFirstChildOfNode(buildingNode, EnumClasses.GMLRelTypes.LOD1_SOLID));
                building.setLod1Solid(lod1Solid);
            }

            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.LOD2_SOLID)) {
                SolidProperty lod2Solid = BoundingBoxCalculator.createSolidProperty(
                        GraphUtil.findFirstChildOfNode(buildingNode, EnumClasses.GMLRelTypes.LOD2_SOLID));
                building.setLod2Solid(lod2Solid);
            }

            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.LOD3_SOLID)) {
                SolidProperty lod3Solid = BoundingBoxCalculator.createSolidProperty(
                        GraphUtil.findFirstChildOfNode(buildingNode, EnumClasses.GMLRelTypes.LOD3_SOLID));
                building.setLod3Solid(lod3Solid);
            }

            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.LOD4_SOLID)) {
                SolidProperty lod4Solid = BoundingBoxCalculator.createSolidProperty(
                        GraphUtil.findFirstChildOfNode(buildingNode, EnumClasses.GMLRelTypes.LOD4_SOLID));
                building.setLod4Solid(lod4Solid);
            }

            // LoD1-4 MultiSurface
            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.LOD1_MULTI_SURFACE)) {
                MultiSurfaceProperty lod1MultiSurface = BoundingBoxCalculator.createMultiSurfaceProperty(
                        GraphUtil.findFirstChildOfNode(buildingNode, EnumClasses.GMLRelTypes.LOD1_MULTI_SURFACE));
                building.setLod1MultiSurface(lod1MultiSurface);
            }

            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.LOD2_MULTI_SURFACE)) {
                MultiSurfaceProperty lod2MultiSurface = BoundingBoxCalculator.createMultiSurfaceProperty(
                        GraphUtil.findFirstChildOfNode(buildingNode, EnumClasses.GMLRelTypes.LOD2_MULTI_SURFACE));
                building.setLod2MultiSurface(lod2MultiSurface);
            }

            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.LOD3_MULTI_SURFACE)) {
                MultiSurfaceProperty lod3MultiSurface = BoundingBoxCalculator.createMultiSurfaceProperty(
                        GraphUtil.findFirstChildOfNode(buildingNode, EnumClasses.GMLRelTypes.LOD3_MULTI_SURFACE));
                building.setLod3MultiSurface(lod3MultiSurface);
            }

            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.LOD4_MULTI_SURFACE)) {
                MultiSurfaceProperty lod4MultiSurface = BoundingBoxCalculator.createMultiSurfaceProperty(
                        GraphUtil.findFirstChildOfNode(buildingNode, EnumClasses.GMLRelTypes.LOD4_MULTI_SURFACE));
                building.setLod4MultiSurface(lod4MultiSurface);
            }

            // LoD2-4 MultiCurve
            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.LOD2_MULTI_CURVE)) {
                MultiCurveProperty lod2MultiCurve = BoundingBoxCalculator.createMultiCurveProperty(
                        GraphUtil.findFirstChildOfNode(buildingNode, EnumClasses.GMLRelTypes.LOD2_MULTI_CURVE));
                building.setLod2MultiCurve(lod2MultiCurve);
            }

            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.LOD3_MULTI_CURVE)) {
                MultiCurveProperty lod3MultiCurve = BoundingBoxCalculator.createMultiCurveProperty(
                        GraphUtil.findFirstChildOfNode(buildingNode, EnumClasses.GMLRelTypes.LOD3_MULTI_CURVE));
                building.setLod3MultiCurve(lod3MultiCurve);
            }

            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.LOD4_MULTI_CURVE)) {
                MultiCurveProperty lod4MultiCurve = BoundingBoxCalculator.createMultiCurveProperty(
                        GraphUtil.findFirstChildOfNode(buildingNode, EnumClasses.GMLRelTypes.LOD4_MULTI_CURVE));
                building.setLod4MultiCurve(lod4MultiCurve);
            }

            // BOUNDED_BY_SURFACE
            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.BOUNDED_BY_SURFACE)) {
                for (Node n : GraphUtil.findChildrenOfNode(buildingNode, EnumClasses.GMLRelTypes.BOUNDED_BY_SURFACE)) {
                    BoundarySurfaceProperty boundarySurface = BoundingBoxCalculator.createBoundarySurfaceProperty(n);
                    building.addBoundedBySurface(boundarySurface);
                }
            }

            // OuterBuildingInstallation
            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.OUTER_BUILDING_INSTALLATION)) {
                for (Node n : GraphUtil.findChildrenOfNode(buildingNode, EnumClasses.GMLRelTypes.OUTER_BUILDING_INSTALLATION)) {
                    BuildingInstallationProperty outerBuildingInstallation = BoundingBoxCalculator.createBuildingInstallationProperty(n);
                    building.addOuterBuildingInstallation(outerBuildingInstallation);
                }
            }

            // BuildingPart
            if (buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.BUILDING_PART)) {
                for (Node n : GraphUtil.findChildrenOfNode(buildingNode, EnumClasses.GMLRelTypes.BUILDING_PART)) {
                    BuildingPartProperty buildingPart = BoundingBoxCalculator.createBuildingPartProperty(n);
                    building.addConsistsOfBuildingPart(buildingPart);

                    // also attach bounding shapes to building parts
                    attachBbox(GraphUtil.findFirstChildOfNode(n, EnumClasses.GMLRelTypes.OBJECT), new BuildingPart());
                }
            }

            if (!buildingNode.hasRelationship(Direction.OUTGOING, EnumClasses.GMLRelTypes.BOUNDED_BY)) {
                createNode(building.calcBoundedBy(true), buildingNode, EnumClasses.GMLRelTypes.BOUNDED_BY);
            }
        }

        if (building instanceof Building) {// exclude BuildingPart

            Envelope envelope = BoundingBoxCalculator.createBoundingShape(GraphUtil.findFirstChildOfNode(buildingNode, EnumClasses.GMLRelTypes.BOUNDED_BY)).getEnvelope();

            double[][] lowerUpperCorner = GeometryUtil.getLowerUpperCorner(envelope, logger);
            double[] lowerCorner = lowerUpperCorner[0];
            double[] upperCorner = lowerUpperCorner[1];

            if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.TILES)) {
                // if city envelope is missing, find lower corners while iterating over buildings
                if (cityBoundedByMissing) {
                    if (lowerCorner[0] < cityEnvelopeLowerX) {
                        cityEnvelopeLowerX = lowerCorner[0];
                    }

                    if (lowerCorner[1] < cityEnvelopeLowerY) {
                        cityEnvelopeLowerY = lowerCorner[1];
                    }

                    if (upperCorner[0] > cityEnvelopeUpperX) {
                        cityEnvelopeUpperX = upperCorner[0];
                    }

                    if (upperCorner[1] > cityEnvelopeUpperY) {
                        cityEnvelopeUpperY = upperCorner[1];
                    }
                } else {
                    assignBuildingsWithEnvelopeToTiles(lowerCorner, upperCorner, buildingNode);
                }
            } else if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.RTREE)) {
                com.vividsolutions.jts.geom.Coordinate lowerCoordinate = new com.vividsolutions.jts.geom.Coordinate(lowerCorner[0], lowerCorner[1]);
                com.vividsolutions.jts.geom.Coordinate upperCoordinate = new com.vividsolutions.jts.geom.Coordinate(upperCorner[0], upperCorner[1]);

                com.vividsolutions.jts.geom.Envelope bbox = new com.vividsolutions.jts.geom.Envelope(lowerCoordinate, upperCoordinate);

                // add the bounding box of this building to the RTree
                Node geomNode = buildingLayer.add(buildingLayer.getGeometryFactory().toGeometry(bbox)).getGeomNode();
                // link the building node to this RTree node to retrieve data later
                geomNode.createRelationshipTo(buildingNode, Matcher.TmpRelTypes.RTREE_DATA);
            }
        }
    }
    */
}
