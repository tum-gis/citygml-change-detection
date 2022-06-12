package components.mapper;

import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.base.AssociationByRepOrRef;
import org.citygml4j.model.gml.base.StringOrRef;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.SETTINGS;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NodeFactory {
    private final static Logger logger = LoggerFactory.getLogger(NodeFactory.class);
    private final GraphDatabaseService graphDb;
    private final IndexDefinition hrefDbIndex;
    private final IndexDefinition idDbIndex;

    // For indexing
    private final String hrefProperty = "href";
    private final String idProperty = "id";
    private final String hrefDbIndexName = "hrefDbIndex";
    private final String idDbIndexName = "idDbIndex";
    private final Label[] hrefNodeLabels = {
            Label.label(AssociationByRepOrRef.class.getName()),
            Label.label(StringOrRef.class.getName())
    };
    private final Label idNodeLabel = Label.label(AbstractGML.class.getName());

    public NodeFactory(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
        // Create automatic indexing for attributes "href" and "id" while creating nodes
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = tx.schema();
            this.hrefDbIndex = schema.indexFor(hrefNodeLabels)
                    .on(hrefProperty)
                    .withName(hrefDbIndexName)
                    .create();
            this.idDbIndex = schema.indexFor(idNodeLabel)
                    .on(idProperty)
                    .withName(idDbIndexName)
                    .create();
            tx.commit();
            logger.info("Created automatic database indexing for XLinks");
        }
    }

    public Node create(Object object) {
        Node result = null;
        try (Transaction tx = graphDb.beginTx()) {
            // Get the object's class
            Class objectClass = object.getClass();

            // Get class hierarchy
            ArrayList<Label> objectClassHierarchyLabels = new ArrayList<>();
            Class tmpObjectClass = objectClass;
            while (tmpObjectClass.getSuperclass() != null) {
                objectClassHierarchyLabels.add(Label.label(tmpObjectClass.toString()));
                tmpObjectClass = tmpObjectClass.getSuperclass();
            }

            // Create a node with labels named after its class hierarchy
            result = tx.createNode((Label[]) objectClassHierarchyLabels.toArray());

            // Get all properties and methods inherited except from Object class
            for (PropertyDescriptor propertyDescriptor
                    : Introspector.getBeanInfo(objectClass, Object.class).getPropertyDescriptors()) {
                Method getter = propertyDescriptor.getReadMethod();
                if (getter == null) {
                    // The current property does not have an accessible getter
                    break;
                }

                if (getter.getReturnType().isPrimitive()) {
                    String propertyName = getter.getName();
                    Object propertyValue = getter.invoke(object);
                    if (propertyName.equals(hrefProperty) && propertyValue.toString().charAt(0) != '#') {
                        logger.warn("Element href = {} without prefix '#' detected, this shall be corrected automatically", propertyValue.toString());
                        result.setProperty(propertyName, "#" + propertyValue);
                    } else {
                        result.setProperty(propertyName, propertyValue);
                    }
                } else if (getter.getReturnType().isArray()) {
                    Object[] values = (Object[]) getter.invoke(object);
                    int count = 0;
                    for (Object v : values) {
                        // Recursively map sub-elements
                        Node vNode = create(v);
                        Relationship rel
                                = result.createRelationshipTo(vNode, RelationshipType.withName(getter.getName()));
                        // Additional metadata
                        rel.setProperty(PropertyFactory.AGGREGATION_TYPE.toString(),
                                PropertyFactory.TYPE_ARRAY.toString());
                        rel.setProperty(PropertyFactory.INDEX.toString(), count++);
                    }
                } else if (Collection.class.isAssignableFrom(getter.getReturnType())) {
                    Collection<?> values = (Collection<?>) getter.invoke(object);
                    int count = 0;
                    for (Object v : values) {
                        // Recursively map sub-elements
                        Node vNode = create(v);
                        Relationship rel
                                = result.createRelationshipTo(vNode, RelationshipType.withName(getter.getName()));
                        // Additional metadata
                        rel.setProperty(PropertyFactory.AGGREGATION_TYPE.toString(),
                                PropertyFactory.TYPE_COLLECTION.toString());
                        rel.setProperty(PropertyFactory.INDEX.toString(), count++);
                    }
                } else if (Map.class.isAssignableFrom(getter.getReturnType())) {
                    // Fill in the node with map entries
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) getter.invoke(object)).entrySet()) {
                        // TODO Store all entries in one single node if they are of primitive type?
                        // Recursively map sub-elements
                        Node entryNode = create(entry.getValue());
                        Relationship rel
                                = result.createRelationshipTo(entryNode, RelationshipType.withName(getter.getName()));
                        // Additional metadata
                        rel.setProperty(PropertyFactory.AGGREGATION_TYPE.toString(),
                                PropertyFactory.TYPE_MAP);
                        rel.setProperty(PropertyFactory.MAP_KEY.toString(), entry.getKey());
                    }
                } else {
                    // Is a complex type
                    Node childNode = create(getter.invoke(object));
                    Relationship rel
                            = result.createRelationshipTo(childNode, RelationshipType.withName(getter.getName()));
                }
            }

            tx.commit();
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException | IntrospectionException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    // This function is called AFTER all hrefs and ids have been stored
    // This function runs in single-threaded mode
    // At the end the indexings shall be dropped
    public void resolveXLinks() {
        // Wait for indexing to finish
        logger.info("Populating database indices ---");
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = tx.schema();
            schema.awaitIndexOnline(hrefDbIndex, 60, TimeUnit.SECONDS);
            schema.awaitIndexOnline(idDbIndex, 60, TimeUnit.SECONDS);
        }
        logger.info("--- Done");

        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = tx.schema();
            logger.debug(String.format("Indexing href complete: %1.0f%%",
                    schema.getIndexPopulationProgress(hrefDbIndex).getCompletedPercentage()));
            logger.debug(String.format("Indexing id complete: %1.0f%%",
                    schema.getIndexPopulationProgress(idDbIndex).getCompletedPercentage()));
        }

        logger.info("Resolving XLinks/hrefs ---");
        if (SETTINGS.ENABLE_INDICES) {
            int countTransactions = SETTINGS.NR_OF_COMMMIT_TRANS;
            Transaction tx = graphDb.beginTx();
            try {
                for (Label label : hrefNodeLabels) {
                    // The value href must ALWAYS begin with "#"
                    try (ResourceIterator<Node> hrefNodes
                                 = tx.findNodes(label, hrefProperty, "#", StringSearchMode.PREFIX)) {
                        while (hrefNodes.hasNext()) {
                            Node hrefNode = hrefNodes.next();
                            String idValue = hrefNode.getProperty(hrefProperty).toString()
                                    .replace("#", "");
                            try (ResourceIterator<Node> idNodes = tx.findNodes(idNodeLabel, idProperty, idValue)) {
                                int idCount = 0;
                                while (idNodes.hasNext()) {
                                    // Periodically commit in batch
                                    if (countTransactions == 0) {
                                        tx.commit();
                                        logger.debug("Committed a batch of {} transactions",
                                                SETTINGS.NR_OF_COMMMIT_TRANS);
                                        countTransactions = SETTINGS.NR_OF_COMMMIT_TRANS;
                                        tx = graphDb.beginTx();
                                    }

                                    Node idNode = idNodes.next();
                                    hrefNode.createRelationshipTo(idNode, RelationshipFactory.HREF);
                                    countTransactions--;
                                    hrefNode.removeProperty(hrefProperty);
                                    countTransactions--;
                                    idCount++;
                                }
                                if (idCount == 0) {
                                    logger.warn("No element with referenced ID = {} found", idValue);
                                } else if (idCount >= 2) {
                                    logger.warn("{} elements of the same ID = {} detected", idCount, idValue);
                                }
                            }
                        }
                    }
                }

                tx.commit();
            } finally {
                tx.close();
            }
        } else {
            // TODO Without using database indexing
        }

        // Remove indexing (in order to not unnecessarily overload the mapping process of new dataset)
        try (Transaction tx = graphDb.beginTx()) {
            hrefDbIndex.drop();
            idDbIndex.drop();
            tx.commit();
        }

        logger.info("XLinks resolved");
    }
}
