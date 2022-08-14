package components.mapper;

import components.Project;
import org.citygml4j.model.citygml.core.CityObjectMember;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.base.AssociationByRepOrRef;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.index.IndexManager;
import org.neo4j.gis.spatial.rtree.RTreeIndex;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.internal.kernel.api.security.SecurityContext;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class IndexFactory {
    private final static Logger logger = LoggerFactory.getLogger(IndexFactory.class);

    // Indexing hrefs
    private final static String[] hrefIndexNames = {
            "index_href_assoc",
            "index_href_string"
    };
    private final static Label idNodeLabel = Label.label(AbstractGML.class.getName());

    // Indexing ids
    private final static String idIndexName = "index_id";
    private final static Label[] hrefNodeLabels = {
            Label.label(AssociationByRepOrRef.class.getName()),
            Label.label(StringOrRef.class.getName())
    };

    // Indexing top-level features
    private final static String featureIndexName = "index_feature";
    private final static Label featureNodeLabel = Label.label(AbstractFeature.class.getName());

    // Auxiliary prefixes for href/id to indicate their origin (needed for resolving XLinks, will be removed after)
    private final static String prefixOld = "*old*";
    private final static String prefixNew = "*new*";

    // Spatial layers for all top-level features such as buildings, bridges, etc.
    private final static String rtreeLayerOld = "oldLayer";
    private final static String rtreeLayerNew = "newLayer";

    // Indexing for both datasets
    public static void initGlobalIndexing(GraphDatabaseService graphDb) {
        try (Transaction tx = graphDb.beginTx()) {
            // Create automatic indexing while creating nodes
            Schema schema = tx.schema();
            // Indexing abstract features
            schema.indexFor(featureNodeLabel)
                    .on(PropertyNameFactory.isOld.toString())
                    .withName(featureIndexName)
                    .create();
            tx.commit();
            logger.info("Created automatic database indexing for abstract features");
        }
    }

    public static void initXLinkIndexing(GraphDatabaseService graphDb) {
        try (Transaction tx = graphDb.beginTx()) {
            // Create automatic indexing while creating nodes
            Schema schema = tx.schema();
            // Indexing hrefs
            for (int i = 0; i < hrefNodeLabels.length; i++) {
                schema.indexFor(hrefNodeLabels[i])
                        .on(PropertyNameFactory.href.toString())
                        .withName(hrefIndexNames[i])
                        .create();
            }
            // Indexing ids
            schema.indexFor(idNodeLabel)
                    .on(PropertyNameFactory.id.toString())
                    .withName(idIndexName)
                    .create();
            tx.commit();
            logger.info("Created automatic database indexing for XLinks");
        }
    }

    // Check if the property is href and whether the value has prefix `#`
    // Then store this href in the node
    public static void setHrefId(Transaction tx, String propertyName, Object propertyValue, boolean isOld, Node node) {
        String stringValue = "";
        if ((propertyName.equals(PropertyNameFactory.href.toString()))
                && (propertyValue.toString().charAt(0) != '#')) {
            logger.warn("Element {} = {} without prefix '#' detected, this shall be corrected automatically", propertyName, propertyValue);
            stringValue = "#" + propertyValue;
        }

        // To indicate whether the elements are from old/new city model
        // -> add prefix e.g. *old*... OR *new*... to href/id
        if ((propertyName.equals(PropertyNameFactory.href.toString()))
                || (propertyName.equals(PropertyNameFactory.id.toString()))) {
            stringValue = (isOld ? prefixOld : prefixNew) + propertyValue;
            node.setProperty(propertyName, stringValue);
        }
    }

    public static IndexDefinition[] getHrefIndices(Transaction tx) {
        IndexDefinition[] result = new IndexDefinition[hrefIndexNames.length];
        Schema schema = tx.schema();
        for (int i = 0; i < result.length; i++) {
            result[i] = schema.getIndexByName(hrefIndexNames[i]);
        }
        return result;
    }

    public static IndexDefinition getIdIndex(Transaction tx) {
        Schema schema = tx.schema();
        return schema.getIndexByName(idIndexName);
    }


    // This function is called AFTER all hrefs and ids have been stored
    // This function runs in single-threaded mode
    // At the end the indexing shall be dropped
    public static void resolveXLinks(GraphDatabaseService graphDb, boolean isOld) {
        // Wait for indexing to finish
        wait(graphDb);

        logger.info("Resolving XLinks/hrefs ---");
        int countTransactions = Project.conf.getBatch().getTrans();
        Transaction tx = graphDb.beginTx();
        try {
            for (Label label : hrefNodeLabels) {
                // The value href must ALWAYS begin with prefix + "#"
                String prefix = (isOld ? prefixOld : prefixNew);
                try (ResourceIterator<Node> hrefNodes = tx.findNodes(label, PropertyNameFactory.href.toString(),
                        prefix + "#", StringSearchMode.PREFIX)) {
                    while (hrefNodes.hasNext()) {
                        Node hrefNode = hrefNodes.next();
                        String idValue = hrefNode.getProperty(PropertyNameFactory.href.toString()).toString()
                                .replace(prefix + "#", "");
                        try (ResourceIterator<Node> idNodes
                                     = tx.findNodes(idNodeLabel, PropertyNameFactory.id.toString(),
                                prefix + idValue, StringSearchMode.EXACT)) {
                            int idCount = 0;
                            while (idNodes.hasNext()) {
                                // Periodically commit in batch
                                if (countTransactions == 0) {
                                    tx.commit();
                                    tx.close();
                                    logger.debug("Committed a batch of {} transactions",
                                            Project.conf.getBatch().getTrans());
                                    countTransactions = Project.conf.getBatch().getTrans();
                                    tx = graphDb.beginTx();
                                }

                                // Connect href parent with id node
                                Node idNode = idNodes.next();
                                Relationship hrefRel
                                        = hrefNode.getRelationships(Direction.INCOMING).iterator().next();
                                Node hrefParentNode = hrefRel.getStartNode();

                                // For cityObjectMembers:
                                // (:CityModel) -[:cityObjectMember]-> (:CityObjectMember.href)
                                //                                              -[:object]-> (:Feature.id)
                                if (hrefRel.getType().name()
                                        .equals(RelationshipFactory.cityObjectMember.toString())) {
                                    hrefNode.createRelationshipTo(idNode, RelationshipFactory.object);
                                } else {
                                    Relationship hrefRelNew
                                            = hrefParentNode.createRelationshipTo(idNode, hrefRel.getType());
                                    // Copy the relationship (and ALL its properties)
                                    for (Map.Entry<String, Object> entry : hrefRel.getAllProperties().entrySet()) {
                                        if (entry.getKey().equals(
                                                PropertyNameFactory.AGGREGATION_MEMBER_TYPE.toString())) {
                                            // Avoid mismatching types
                                            // -> Update type of the element containing id (and not href)
                                            hrefRelNew.setProperty(entry.getKey(),
                                                    idNode.getProperty(PropertyNameFactory.definingClass.toString()));
                                        } else {
                                            hrefRelNew.setProperty(entry.getKey(), entry.getValue());
                                        }
                                    }
                                }
                                // Remove auxiliary prefix from id
                                idNode.setProperty(PropertyNameFactory.id.toString(), idValue);
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
            logger.info("--- Done");
        } finally {
            tx.close();
        }
    }

    private static void wait(GraphDatabaseService graphDb) {
        logger.info("Populating database indices ---");
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = tx.schema();
            for (String name : hrefIndexNames) {
                IndexDefinition hrefDbIndex = schema.getIndexByName(name);
                schema.awaitIndexOnline(hrefDbIndex, 60, TimeUnit.SECONDS);
            }
            IndexDefinition idDbIndex = schema.getIndexByName(idIndexName);
            schema.awaitIndexOnline(idDbIndex, 60, TimeUnit.SECONDS);
            tx.commit();
            logger.info("--- Done");
        }
    }

    // Remove indexing (in order to not unnecessarily overload the mapping process of new dataset)
    public static void dropHrefs(GraphDatabaseService graphDb, boolean isOld) {
        logger.info("Dropping href nodes and indexing");
        int stepsTillCommit = Project.conf.getBatch().getTrans();
        Transaction tx = graphDb.beginTx();
        try {
            // Remove href nodes
            for (Label label : hrefNodeLabels) {
                // The value href must ALWAYS begin with prefix + "#"
                String prefix = (isOld ? prefixOld : prefixNew);
                try (ResourceIterator<Node> hrefNodes
                             = tx.findNodes(label, PropertyNameFactory.href.toString(),
                        prefix + "#", StringSearchMode.PREFIX)) {
                    while (hrefNodes.hasNext()) {
                        // Periodically commit in batch
                        if (stepsTillCommit == 0) {
                            tx.commit();
                            tx.close();
                            logger.debug("Committed a batch of {} transactions",
                                    Project.conf.getBatch().getTrans());
                            stepsTillCommit = Project.conf.getBatch().getTrans();
                            tx = graphDb.beginTx();
                        }

                        Node hrefNode = hrefNodes.next();
                        if (hrefNode.getProperty(PropertyNameFactory.definingClass.toString())
                                .equals(CityObjectMember.class.getName())) {
                            // Remove property href
                            hrefNode.removeProperty(PropertyNameFactory.href.toString());
                            stepsTillCommit--;
                            // Remove all outgoing edges and nodes except the one for top-level features
                            for (Relationship rel : hrefNode.getRelationships(Direction.OUTGOING)) {
                                if (!rel.getType().name().equals(RelationshipFactory.object.toString())) {
                                    Node endNode = rel.getEndNode();
                                    rel.delete();
                                    stepsTillCommit--;
                                    endNode.delete();
                                    stepsTillCommit--;
                                }
                            }
                        } else {
                            // Only one incoming edge
                            Relationship hrefRel = hrefNode.getRelationships(Direction.INCOMING).iterator().next();
                            hrefRel.delete();
                            stepsTillCommit--;
                            // Might be multiple outgoing edges (e.g. getAssociationClass)
                            for (Relationship rel : hrefNode.getRelationships(Direction.OUTGOING)) {
                                Node endNode = rel.getEndNode();
                                rel.delete();
                                stepsTillCommit--;
                                endNode.delete();
                                stepsTillCommit--;
                            }
                            hrefNode.delete();
                            stepsTillCommit--;
                        }
                    }
                }
            }
            tx.commit();
        } finally {
            tx.close();
        }

        // Separate transaction for schema operations
        try (Transaction txIndex = graphDb.beginTx()) {
            // Drop indexing for hrefs
            for (IndexDefinition index : getHrefIndices(txIndex)) {
                index.drop();
            }
            getIdIndex(txIndex).drop();
            txIndex.commit();
        }

        logger.info("--- Done");
    }

    private static void delete(Node node) {

    }

    public static void initRTreeLayer(GraphDatabaseService graphDb, boolean isOld) {
        try (Transaction tx = graphDb.beginTx()) {
            // Init an R-Tree layer for each dataset
            SpatialDatabaseService spatialDb = new SpatialDatabaseService(
                    new IndexManager((GraphDatabaseAPI) graphDb, SecurityContext.AUTH_DISABLED));
            EditableLayer rtreeLayer = spatialDb.getOrCreateEditableLayer(tx,
                    (isOld ? rtreeLayerOld : rtreeLayerNew));
            // Set config to this layer
            Map<String, Object> config = new HashMap<>();
            config.put(RTreeIndex.KEY_MAX_NODE_REFERENCES, Project.conf.getRtree().getNodeRef());
            rtreeLayer.getIndex().configure(config);
            // TODO Add bbox to layer
            tx.commit();
            logger.info("Initiated RTree layers");
        }
    }
}
