package util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.geometry.primitives.Envelope;
import org.geotools.data.neo4j.Neo4jFeatureBuilder;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.gis.spatial.rtree.RTreeImageExporter;
import org.neo4j.gis.spatial.rtree.RTreeIndex;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.opengis.feature.simple.SimpleFeatureType;

import mapper.BoundingBoxCalculator;
import mapper.EnumClasses.GMLRelTypes;
import matcher.EditOperationEnums.DeletePropertyNodeProperties;
import matcher.EditOperationEnums.DeleteRelationshipNodeProperties;
import matcher.Matcher.EditOperators;
import matcher.Matcher.EditRelTypes;
import matcher.EditOperationEnums.InsertPropertyNodeProperties;
import matcher.EditOperationEnums.InsertRelationshipNodeProperties;
import matcher.Matcher.TmpRelTypes;
import matcher.EditOperationEnums.UpdatePropertyNodeProperties;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 *
 */
public class GraphUtil {
	/**
	 * Nodes are matched from top to bottom, which means they ALL should be matched
	 * until the BUILDING level is reached, in which case only then the given types
	 * shall be matched
	 */
	public static boolean SHOULD_APPLY_MATCH_ONLY = false;
	
	/**
	 * Find a node by its ID.
	 * 
	 * Return null if there is no node or there are more than 2 nodes found.
	 * 
	 * Return a node if there is exactly one node found.
	 * 
	 * (If there are two nodes found, the one belongs to respective city model is returned.)
	 * 
	 * @param indexManager
	 * @param id
	 * @param logger
	 * @return
	 */
	public static Node findNodeById(ConcurrentHashMap<String, Node> idIndex, String id, Logger logger) {
		Node foundNode = idIndex.get(id);

		if (foundNode == null) {
			logger.warning("WARNING: COULD NOT FIND NODE WITH ID = " + id + ". HREF IGNORED...");
			return null;
		}

		return foundNode;

		// ArrayList<Node> foundNodes = new ArrayList<Node>();
		//
		// Iterator<Entry<String, Node>> it = idIndex.entrySet().iterator();
		// while (it.hasNext()) {
		// Map.Entry<String, Node> pair = (Map.Entry<String, Node>) it.next();
		//
		// if (pair.getKey().equals(id)) {
		// foundNodes.add(pair.getValue());
		// it.remove();
		// break;
		// }
		// }
		//
		// // for (Node n : idIndex.query("id", id)) {
		// // foundNodes.add(n);
		// // }
		//
		// switch (foundNodes.size()) {
		// case 0:
		// logger.warning("WARNING: COULD NOT FIND NODE WITH ID = " + id);
		// return null;
		// case 1:
		// return foundNodes.get(0);
		// case 2:
		// if (foundNodes.get(0).getId() < foundNodes.get(1).getId()) {
		// if (isOld) {
		// return foundNodes.get(0);
		// }
		// return foundNodes.get(1);
		// } else {
		// if (isOld) {
		// return foundNodes.get(1);
		// }
		// return foundNodes.get(0);
		// }
		// default:
		// logger.warning("WARNING: MULTIPLE NODES HAVE THE SAME ID = " + id);
		// return null;
		// }
	}

	public static Node findNodeById(Index<Node> idApiIndex, String id, Logger logger) {
		Node foundNode = null;
		try {
			foundNode = idApiIndex.get("id", id).getSingle();
		} catch (NoSuchElementException exception) {
			logger.warning("WARNING: MULTIPLE ELEMENTS EXIST WITH ID = " + id + ". HREF IGNORED...");
			return null;
		}

		if (foundNode == null) {
			logger.warning("WARNING: COULD NOT FIND NODE WITH ID = " + id + ". HREF IGNORED...");
		}

		return foundNode;
	}

	public static String getLabelString(Node node) {
		return node.getLabels().iterator().next().toString();
	}

	public static ArrayList<Node> findChildrenOfNode(Node parent, Label label, RelationshipType... avoidRelTypes) {
		ArrayList<Node> results = new ArrayList<Node>();

		for (Relationship rel : parent.getRelationships(Direction.OUTGOING)) {
			for (RelationshipType avoidRelType : avoidRelTypes) {
				if (rel.getType().toString().equals(avoidRelType.toString())) {
					continue;
				}
			}

			Node child = rel.getOtherNode(parent);

			if (label != null && !child.hasLabel(label)) {
				continue;
			}

			results.add(child);
		}

		return results;
	}

	public static ArrayList<Node> findChildrenOfNode(Node parent, RelationshipType... avoidRelTypes) {
		return findChildrenOfNode(parent, null, avoidRelTypes);
	}

	public static ArrayList<Node> findChildrenOfNode(Node parent) {
		return findChildrenOfNode(parent, null, new RelationshipType[] {});
	}

	/**
	 * Return the first child that has a given label.
	 * 
	 * @param parent
	 * @param label
	 * @return
	 */
	public static Node findFirstChildOfNode(Node parent, Label label) {
		for (Relationship rel : parent.getRelationships(Direction.OUTGOING)) {
			Node child = rel.getOtherNode(parent);
			if (child.hasLabel(label)) {
				return child;
			}
		}

		return null;
	}

	/**
	 * Return children of a given label.
	 * 
	 * @param parent
	 * @param label
	 * @return
	 */
	public static ArrayList<Node> findChildrenOfNode(Node parent, Label label) {
		ArrayList<Node> result = new ArrayList<Node>();
		for (Relationship rel : parent.getRelationships(Direction.OUTGOING)) {
			Node child = rel.getOtherNode(parent);
			if (child.hasLabel(label)) {
				result.add(child);
			}
		}

		return result;
	}

	/**
	 * Return the first child of a given relationship type.
	 * 
	 * @param parent
	 * @param relType
	 * @return
	 */
	public static Node findFirstChildOfNode(Node parent, RelationshipType relType) {
		for (Relationship rel : parent.getRelationships(Direction.OUTGOING)) {
			if (rel.getType().toString().equals(relType.toString())) {
				return rel.getOtherNode(parent);
			}
		}

		return null;
	}

	public static Node findFirstParentOfNode(Node child, RelationshipType relType) {
		for (Relationship rel : child.getRelationships(Direction.INCOMING)) {
			if (rel.getType().toString().equals(relType.toString())) {
				return rel.getOtherNode(child);
			}
		}

		return null;
	}

	/**
	 * Return a list of children given a relationship type.
	 * 
	 * @param parent
	 * @param relType
	 * @return
	 */
	public static ArrayList<Node> findChildrenOfNode(Node parent, RelationshipType relType) {
		ArrayList<Node> result = new ArrayList<Node>();
		for (Relationship rel : parent.getRelationships(Direction.OUTGOING)) {
			if (rel.getType().toString().equals(relType.toString())) {
				result.add(rel.getOtherNode(parent));
			}
		}

		return result;
	}

	public static ArrayList<Node> findSortedChildrenOfNode(Node parent, RelationshipType relType) {
		ArrayList<Node> result = findChildrenOfNode(parent, relType);

		// sort in order of nodes' ids
		Collections.sort(result, new Comparator<Node>() {
			@Override
			public int compare(Node n1, Node n2) {
				return (int) (n1.getId() - n2.getId());
			}
		});

		return result;
	}

	public static ArrayList<Node> findParentsOfNode(Node child, Label label) {
		ArrayList<Node> result = new ArrayList<Node>();
		for (Relationship rel : child.getRelationships(Direction.INCOMING)) {
			Node parent = rel.getOtherNode(child);

			if (parent.hasLabel(label)) {
				result.add(parent);
			}
		}

		return result;
	}

	public static ArrayList<Node> findParentsOfNode(Node child, RelationshipType relType) {
		ArrayList<Node> result = new ArrayList<Node>();
		for (Relationship rel : child.getRelationships(Direction.INCOMING)) {
			if (rel.getType().toString().equals(relType.toString())) {
				result.add(rel.getOtherNode(child));
			}
		}

		return result;
	}

	/**
	 * Find (unique) outgoing relationship types of a node.
	 * 
	 * Note that a node can have multiple relationships of the same relationship type.
	 * 
	 * @param node
	 * @return
	 */
	public static Iterable<RelationshipType> findRelationshipTypes(Node node) {
		ArrayList<RelationshipType> result = new ArrayList<RelationshipType>();

		for (Relationship rel : node.getRelationships(Direction.OUTGOING)) {
			if (!result.contains(rel.getType())) {
				result.add(rel.getType());
			}
		}

		return result;
	}

	/**
	 * Find (unique) outgoing relationship types of a node, excluding a given relationship type.
	 * 
	 * Note that a node can have multiple relationships of the same relationship type.
	 * 
	 * @param node
	 * @param ignoreRelType
	 *            null if none is excluded
	 * @return
	 */
	public static Iterable<RelationshipType> findRelationshipTypesExclude(Node node, RelationshipType ignoreRelType) {
		ArrayList<RelationshipType> result = new ArrayList<RelationshipType>();

		for (Relationship rel : node.getRelationships(Direction.OUTGOING)) {
			if (ignoreRelType != null && rel.getType().toString().equals(ignoreRelType.toString())) {
				continue;
			}

			if (!result.contains(rel.getType())) {
				// Find only relationship types defined by users in the config file
				if (SHOULD_APPLY_MATCH_ONLY && !SETTINGS.MATCH_ONLY.equals("")) {
					String[] onlyTypes = SETTINGS.MATCH_ONLY.split(" ");
					for (String s : onlyTypes) {
						if (rel.getType().toString().equals(SETTINGS.MATCH_ONLY)) {
							result.add(rel.getType());
							break;
						}
					}
				} else {
					result.add(rel.getType());
				}
			}
		}

		return result;
	}

	/**
	 * Count all nodes of all labels.
	 * 
	 * @param graphDb
	 * @param targetlabels
	 *            null for all labels (except from ignoreLabels)
	 * @param ignoreLabels
	 *            null if none is ignored
	 * @return
	 */
	public static HashMap<String, Long> countAllNodesWithLabels(GraphDatabaseService graphDb, Transaction tx, ArrayList<Label> targetlabels, ArrayList<Label> ignoreLabels) {
		HashMap<String, Long> hashMapStats = new HashMap<String, Long>();

		// save labels into a non ResourceIterable so that a transaction can be closed while iterating over them
		ArrayList<Label> labels = new ArrayList<Label>();
		for (Label label : graphDb.getAllLabelsInUse()) {
			labels.add(label);
		}

		for (Label label : labels) {
			long nodeCount = 0;

			if (targetlabels != null && !targetlabels.contains(label)) {
				continue;
			}

			if (ignoreLabels != null && ignoreLabels.contains(label)) {
				continue;
			}

			ResourceIterator<Node> countIt = graphDb.findNodes(label);
			while (countIt.hasNext()) {
				nodeCount++;

//				if (nodeCount % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
//					tx.success();
//					tx.close();
//					tx = graphDb.beginTx();
//				}

				countIt.next();
			}

			hashMapStats.put(label.toString(), nodeCount);
		}

		return hashMapStats;
	}

	/**
	 * Return a list of buildings in the given city model.
	 * 
	 * @param cityModel
	 * @return
	 */
	public static ArrayList<Node> findBuildings(Node cityModel) {
		ArrayList<Node> buildings = new ArrayList<Node>();

		for (Node cityObjectMember : findChildrenOfNode(cityModel, Label.label(CityGMLClass.CITY_OBJECT_MEMBER + ""))) {
			// each city object member can have max 1 building
			ArrayList<Node> nodes = findChildrenOfNode(cityObjectMember, Label.label(CityGMLClass.BUILDING + ""));
			if (nodes.size() > 0) {
				buildings.add(nodes.get(0));
			}
		}

		return buildings;
	}

	/**
	 * Return the lower corner of the bounding shape of a given building.
	 * 
	 * @param building
	 * @return
	 */
	public static Node getLowerCornerBoundingShapeOfBuilding(Node building) {
		Node boundingShape = findFirstChildOfNode(building, GMLRelTypes.BOUNDED_BY);
		if (boundingShape == null) {
			return null;
		}

		Node envelope = findFirstChildOfNode(boundingShape, GMLRelTypes.ENVELOPE);
		if (envelope == null) {
			return null;
		}

		return findFirstChildOfNode(envelope, GMLRelTypes.LOWER_CORNER);
	}

	/**
	 * Return the upper corner of the bounding shape of a given building.
	 * 
	 * @param building
	 * @return
	 */
	public static Node getUpperCornerBoundingShapeOfBuilding(Node building) {
		Node boundingShape = findFirstChildOfNode(building, GMLRelTypes.BOUNDED_BY);
		if (boundingShape == null) {
			return null;
		}

		Node envelope = findFirstChildOfNode(boundingShape, GMLRelTypes.ENVELOPE);
		if (envelope == null) {
			return null;
		}

		return findFirstChildOfNode(envelope, GMLRelTypes.UPPER_CORNER);
	}

	/**
	 * Search for a building whose spatial location shares the most with the given building.
	 * 
	 * @param buildingNode
	 *            a reference building, whose envelop will be used to find the other building
	 * @param ohterBuildingLayer
	 *            an EditableLayer contains an RTree of buildings' envelopes
	 * @param logger
	 * @return
	 */
	public static Node findBuildingInRTree(Node buildingNode, EditableLayer ohterBuildingLayer, Logger logger, GraphDatabaseService graphDb) {
		Envelope envelope = BoundingBoxCalculator.createBoundingShape(GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.BOUNDED_BY)).getEnvelope();

		double[][] lowerUpperCorner = GeometryUtil.getLowerUpperCorner(envelope, logger);
		double[] lowerCorner = lowerUpperCorner[0];
		double[] upperCorner = lowerUpperCorner[1];

		com.vividsolutions.jts.geom.Coordinate lowerCoordinate = new com.vividsolutions.jts.geom.Coordinate(lowerCorner[0], lowerCorner[1]);
		com.vividsolutions.jts.geom.Coordinate upperCoordinate = new com.vividsolutions.jts.geom.Coordinate(upperCorner[0], upperCorner[1]);

		com.vividsolutions.jts.geom.Envelope bbox = new com.vividsolutions.jts.geom.Envelope(lowerCoordinate, upperCoordinate);

		List<SpatialDatabaseRecord> results = GeoPipeline
				.startIntersectSearch(ohterBuildingLayer, ohterBuildingLayer.getGeometryFactory().toGeometry(bbox))
				.toSpatialDatabaseRecordList();

		if (results.size() == 0) {
			return null;
		}

		double buildingVolume = SETTINGS.MATCH_BUILDINGS_BY_SHARED_VOLUME
				? GeometryUtil.calcBoxVol(lowerCorner, upperCorner)
				: GeometryUtil.calcFootprint(lowerCorner, upperCorner);

		// in case more buildings have the same bounding box
		ArrayList<Node> foundBuildings = new ArrayList<Node>();

		for (SpatialDatabaseRecord result : results) {
			Node tmpBuildingNode = findFirstChildOfNode(result.getGeomNode(), TmpRelTypes.RTREE_DATA);

			Envelope tmpEnvelope = BoundingBoxCalculator.createBoundingShape(GraphUtil.findFirstChildOfNode(tmpBuildingNode, GMLRelTypes.BOUNDED_BY)).getEnvelope();

			double[][] tmpLowerUpperCorner = GeometryUtil.getLowerUpperCorner(tmpEnvelope, logger);
			double[] tmpLowerCorner = tmpLowerUpperCorner[0];
			double[] tmpUpperCorner = tmpLowerUpperCorner[1];

			double otherVolume = SETTINGS.MATCH_BUILDINGS_BY_SHARED_VOLUME
					? GeometryUtil.calcBoxVol(tmpLowerCorner, tmpUpperCorner)
					: GeometryUtil.calcFootprint(tmpLowerCorner, tmpUpperCorner);
			double tmpSharedVolume = SETTINGS.MATCH_BUILDINGS_BY_SHARED_VOLUME
					? GeometryUtil.calcSharedVolOfBoxes(lowerCorner, upperCorner, tmpLowerCorner, tmpUpperCorner)
					: GeometryUtil.calcSharedFootprint(lowerCorner, upperCorner, tmpLowerCorner, tmpUpperCorner);

			double buildingRatio = tmpSharedVolume / buildingVolume;
			double otherRatio = tmpSharedVolume / otherVolume;
			double minOfBothRatios = Math.min(buildingRatio, otherRatio);

			if (minOfBothRatios >= SETTINGS.BUILDING_SHARED_VOL_PERCENTAGE_THRESHOLD) {
				// either found a correct building or two different buildings have similar spatial locations
				foundBuildings.add(tmpBuildingNode);
			}
		}

		Node foundBuilding = null;
		if (foundBuildings.size() == 1) {
			foundBuilding = foundBuildings.get(0);
		} else {
			// if two different buildings have similar spatial locations -> choose the one with the same ID as reference building
			String buildingId = buildingNode.getProperty("id").toString();
			for (Node tmpBuilding : foundBuildings) {
				if (tmpBuilding.getProperty("id").toString().equals(buildingId)) {
					foundBuilding = tmpBuilding;
					break;
				}
			}
		}

		// create geo matched info node
		if (SETTINGS.CREATE_MATCHED_GEOMETRY_NODE && foundBuilding != null) {
			Node node = graphDb.createNode(Label.label("GEOMETRY_MATCHED"));

			node.createRelationshipTo(buildingNode, TmpRelTypes.GEOMETRY_MATCHED);
			node.createRelationshipTo(foundBuilding, TmpRelTypes.GEOMETRY_MATCHED);
		}

		if (foundBuilding == null || !buildingNode.getProperty("id").toString().equals(foundBuilding.getProperty("id").toString())) {
			logger.fine("Matched " + buildingNode.getProperty("id") + " with " + (foundBuilding == null ? "null" : foundBuilding.getProperty("id")));
		}

		return foundBuilding;
	}

	/**
	 * Export RTree signatures as images.
	 * 
	 * @param buildingLayer
	 * @param graphDb
	 * @param filename
	 * @throws IOException
	 */
	public static void exportRTreeImage(EditableLayer buildingLayer, GraphDatabaseService graphDb, String filename) throws IOException {
		SimpleFeatureType featureType = Neo4jFeatureBuilder.getTypeFromLayer(buildingLayer);
		org.neo4j.gis.spatial.rtree.Envelope bbox = buildingLayer.getIndex().getBoundingBox();
		com.vividsolutions.jts.geom.Coordinate lowerCoordinate = new com.vividsolutions.jts.geom.Coordinate(bbox.getMinX(), bbox.getMinY());
		com.vividsolutions.jts.geom.Coordinate upperCoordinate = new com.vividsolutions.jts.geom.Coordinate(bbox.getMaxX(), bbox.getMaxY());
		Node rTreeRootNode = ((RTreeIndex) buildingLayer.getIndex()).getIndexRoot();
		// or Node rTreeRootNode = GraphUtil.findFirstChildOf(buildingLayer.getLayerNode(), RTreeRelationshipTypes.RTREE_ROOT);

		RTreeImageExporter imageExporter = new RTreeImageExporter(
				buildingLayer.getGeometryFactory(),
				buildingLayer.getGeometryEncoder(),
				buildingLayer.getCoordinateReferenceSystem(),
				featureType,
				(RTreeIndex) buildingLayer.getIndex(),
				lowerCoordinate,
				upperCoordinate);

		imageExporter.saveRTreeLayers(new File(filename), rTreeRootNode, Integer.MAX_VALUE);
	}

	public static boolean isAttachedWithNonOptionalEditor(Node node) {
		for (Relationship rel : node.getRelationships(Direction.INCOMING, EditRelTypes.values())) {
			if (!isEditorOptional(rel.getOtherNode(node))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Return a list of editors node that is attached to the given node.
	 * 
	 * @param node
	 * @return node
	 */
	public static ArrayList<Node> getNonOptionalAttachedEditors(Node node) {
		ArrayList<Node> result = new ArrayList<Node>();
		for (Relationship rel : node.getRelationships(Direction.INCOMING, EditRelTypes.values())) {
			Node tmp = rel.getOtherNode(node);
			if (!isEditorOptional(tmp)) {
				result.add(tmp);
			}
		}

		return result;
	}

	public static ArrayList<Node> getNonOptionalAttachedEditors(Node node, Label label) {
		ArrayList<Node> result = new ArrayList<Node>();
		for (Relationship rel : node.getRelationships(Direction.INCOMING, EditRelTypes.values())) {
			Node tmp = rel.getOtherNode(node);
			if (!isEditorOptional(tmp) && tmp.hasLabel(label)) {
				result.add(tmp);
			}
		}

		return result;
	}

	/**
	 * Check if a node and its children have editors attached.
	 * 
	 * @param node
	 * @return
	 */
	public static boolean hasNonOptionalEditors(Node node) {
		if (isAttachedWithNonOptionalEditor(node)) {
			return true;
		}

		for (Node child : findChildrenOfNode(node)) {
			if (hasNonOptionalEditors(child)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Return true if this relationship type points to a simple element (eg. non-geometric) of given building.
	 * 
	 * @param building
	 * @param relType
	 * @return
	 */
	public static boolean isSimpleBuildingElement(Node building, String relType) {
		return relType.toString().equals(GMLRelTypes.CLAZZ + "")
				|| relType.equals(GMLRelTypes.FUNCTION + "")
				|| relType.equals(GMLRelTypes.USAGE + "")
				|| relType.equals(GMLRelTypes.ROOF_TYPE + "")
				|| relType.equals(GMLRelTypes.MEASURED_HEIGHT + "")
				|| relType.equals(GMLRelTypes.STOREY_HEIGHTS_ABOVE_GROUND + "")
				|| relType.equals(GMLRelTypes.STOREY_HEIGHTS_BELOW_GROUND + "")
				|| relType.equals(GMLRelTypes.DESCRIPTION + "")
				|| relType.equals(GMLRelTypes.NAME + "")
				|| relType.equals(GMLRelTypes.EXTERNAL_REFERENCE + "")
				|| relType.equals(GMLRelTypes.GENERIC_ATTRIBUTE + "")

				// geometry
				|| relType.equals(GMLRelTypes.LOD1_SOLID + "")
				|| relType.equals(GMLRelTypes.LOD2_SOLID + "")
				|| relType.equals(GMLRelTypes.LOD3_SOLID + "")
				|| relType.equals(GMLRelTypes.LOD4_SOLID + "")

				|| relType.equals(GMLRelTypes.LOD1_TERRAIN_INTERSECTION + "")
				|| relType.equals(GMLRelTypes.LOD2_TERRAIN_INTERSECTION + "")
				|| relType.equals(GMLRelTypes.LOD3_TERRAIN_INTERSECTION + "")
				|| relType.equals(GMLRelTypes.LOD4_TERRAIN_INTERSECTION + "")

				|| relType.equals(GMLRelTypes.LOD2_MULTI_CURVE + "")
				|| relType.equals(GMLRelTypes.LOD3_MULTI_CURVE + "")
				|| relType.equals(GMLRelTypes.LOD4_MULTI_CURVE + "")

				|| relType.equals(GMLRelTypes.LOD0_FOOT_PRINT + "")
				|| relType.equals(GMLRelTypes.LOD0_ROOF_EDGE + "")

				|| relType.equals(GMLRelTypes.LOD1_MULTI_SURFACE + "")
				|| relType.equals(GMLRelTypes.LOD2_MULTI_SURFACE + "")
				|| relType.equals(GMLRelTypes.LOD3_MULTI_SURFACE + "")
				|| relType.equals(GMLRelTypes.LOD4_MULTI_SURFACE + "")

				|| relType.equals(GMLRelTypes.RELATIVE_TO_TERRAIN + "")
				|| relType.equals(GMLRelTypes.RELATIVE_TO_WATER + "");
	}

	public static boolean isEditorOptional(Node editor) {
		if (SETTINGS.EXECUTE_OPTIONAL) {
			return false;
		}

		if (editor.hasLabel(Label.label(EditOperators.INSERT_PROPERTY + ""))) {
			return Boolean.parseBoolean(editor.getProperty(InsertPropertyNodeProperties.IS_OPTIONAL.toString()).toString());
		}

		if (editor.hasLabel(Label.label(EditOperators.UPDATE_PROPERTY + ""))) {
			return Boolean.parseBoolean(editor.getProperty(UpdatePropertyNodeProperties.IS_OPTIONAL.toString()).toString());
		}

		if (editor.hasLabel(Label.label(EditOperators.DELETE_PROPERTY + ""))) {
			return Boolean.parseBoolean(editor.getProperty(DeletePropertyNodeProperties.IS_OPTIONAL.toString()).toString());
		}

		if (editor.hasLabel(Label.label(EditOperators.INSERT_NODE + ""))) {
			return Boolean.parseBoolean(editor.getProperty(InsertRelationshipNodeProperties.IS_OPTIONAL.toString()).toString());
		}

		if (editor.hasLabel(Label.label(EditOperators.DELETE_NODE + ""))) {
			return Boolean.parseBoolean(editor.getProperty(DeleteRelationshipNodeProperties.IS_OPTIONAL.toString()).toString());
		}

		return false;
	}
}
