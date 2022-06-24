package components.mapper;

import components.Project;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.base.AssociationByRepOrRef;
import org.citygml4j.model.gml.base.StringOrRef;
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
    private final static String hrefProperty = "href";
    private final static String[] hrefIndexNames = {
            "index_href_assoc",
            "index_href_string"
    };
    private final static Label idNodeLabel = Label.label(AbstractGML.class.getName());

    // Indexing ids
    private final static String idProperty = "id";
    private final static String idIndexName = "index_id";
    private final static Label[] hrefNodeLabels = {
            Label.label(AssociationByRepOrRef.class.getName()),
            Label.label(StringOrRef.class.getName())
    };

    // Spatial layers for all top-level features such as buildings, bridges, etc.
    private final static String rtreeLayerOld = "oldLayer";
    private final static String rtreeLayerNew = "newLayer";

    public static void initXLinkIndexing(GraphDatabaseService graphDb) {
        try (Transaction tx = graphDb.beginTx()) {
            // Create automatic indexing while creating nodes
            Schema schema = tx.schema();
            // Indexing hrefs
            for (int i = 0; i < hrefNodeLabels.length; i++) {
                IndexDefinition ind = schema.indexFor(hrefNodeLabels[i])
                        .on(hrefProperty)
                        .withName(hrefIndexNames[i])
                        .create();
            }
            // Indexing ids
            schema.indexFor(idNodeLabel)
                    .on(idProperty)
                    .withName(idIndexName)
                    .create();
            tx.commit();
            logger.info("Created automatic database indexing for XLinks");
        }
    }

    // Check if the property is href and whether the value has prefix `#`
    // Then store this href in the node
    public static void setHref(Transaction tx, String propertyName, Object propertyValue, Node node) {
        if (propertyName.equals(hrefProperty) && propertyValue.toString().charAt(0) != '#') {
            logger.warn("Element href = {} without prefix '#' detected, this shall be corrected automatically", propertyValue);
            node.setProperty(propertyName, "#" + propertyValue);
        } else {
            node.setProperty(propertyName, propertyValue);
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
    public static void resolveXLinks(GraphDatabaseService graphDb) {
        // Wait for indexing to finish
        wait(graphDb);

        logger.info("Resolving XLinks/hrefs ---");
        int countTransactions = Project.conf.getMultithreading().getBatch().getTrans();
        Transaction tx = graphDb.beginTx();
        try {
            for (Label label : hrefNodeLabels) {
                // The value href must ALWAYS begin with "#"
                try (ResourceIterator<Node> hrefNodes
                             = tx.findNodes(label, hrefProperty, "#", StringSearchMode.PREFIX)) {
                    while (hrefNodes.hasNext()) {
                        Node hrefNode = hrefNodes.next();
                        String idValue = hrefNode.getProperty(hrefProperty).toString().replace("#", "");
                        try (ResourceIterator<Node> idNodes = tx.findNodes(idNodeLabel, idProperty, idValue)) {
                            int idCount = 0;
                            while (idNodes.hasNext()) {
                                // Periodically commit in batch
                                if (countTransactions == 0) {
                                    tx.commit();
                                    tx.close();
                                    logger.debug("Committed a batch of {} transactions",
                                            Project.conf.getMultithreading().getBatch().getTrans());
                                    countTransactions = Project.conf.getMultithreading().getBatch().getTrans();
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
            logger.info("--- XLinks resolved");
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
    public static void dropXLinkIndexing(GraphDatabaseService graphDb) {
        try (Transaction tx = graphDb.beginTx()) {
            for (IndexDefinition index : getHrefIndices(tx)) {
                index.drop();
            }
            getIdIndex(tx).drop();
            tx.commit();
            logger.info("Dropped indexing for XLinks");
        }
    }

    public static void initRTreeLayer(GraphDatabaseService graphDb, boolean isOld) {
        try (Transaction tx = graphDb.beginTx()) {
            // Init an R-Tree layer for each dataset
            SpatialDatabaseService spatialDb = new SpatialDatabaseService(
                    new IndexManager((GraphDatabaseAPI) graphDb, SecurityContext.AUTH_DISABLED));
            EditableLayer buildingLayer = spatialDb.getOrCreateEditableLayer(tx,
                    (isOld ? rtreeLayerOld : rtreeLayerNew));
            // Set config to this layer
            Map<String, Object> config = new HashMap<>();
            config.put(RTreeIndex.KEY_MAX_NODE_REFERENCES, Project.conf.getRtree().getNodeRef());
            buildingLayer.getIndex().configure(config);
            tx.commit();
            logger.info("Initiated RTree layers");
        }
    }
}
