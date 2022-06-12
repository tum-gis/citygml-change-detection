package util;

import components.mapper.NodeFactory;
import components.mapper.RelationshipFactory;
import matcher.Matcher;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.xml.io.reader.*;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.beans.IntrospectionException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 */
public class ProducerConsumerUtil {
    static class PoisonPillXMLChunk implements XMLChunk {

        /**
         * Used as end signal.
         */
        public PoisonPillXMLChunk() {
        }

        @Override
        public CityGML unmarshal() throws UnmarshalException, MissingADESchemaException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public QName getTypeName() {
            return null;
        }

        @Override
        public CityGMLClass getCityGMLClass() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isSetParentInfo() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public ParentInfo getParentInfo() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean hasPassedXMLValidation() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void send(ContentHandler handler, boolean release) throws SAXException {
            // TODO Auto-generated method stub

        }
    }

    public static class XMLChunkProducer implements Runnable {
        private final CityGMLReader reader;
        private final BlockingQueue<XMLChunk> queue;

        public XMLChunkProducer(CityGMLReader reader, BlockingQueue<XMLChunk> queue) {
            this.reader = reader;
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                synchronized (reader) {
                    while (reader.hasNext()) {
                        queue.put(reader.nextChunk());
                    }

                    // end signal by having each producer place Nconsumers pills on the queue
                    // and having the consumer stop only when it receives Nproducers pills
                    for (int i = 0; i < SETTINGS.CONSUMERS_PRO_PRODUCER * SETTINGS.NR_OF_PRODUCERS; i++) {
                        queue.put(new PoisonPillXMLChunk());
                    }
                }
            } catch (CityGMLReadException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class XMLChunkConsumer implements Runnable {
        private final static Logger logger = LoggerFactory.getLogger(XMLChunkConsumer.class);
        private static AtomicInteger count = new AtomicInteger();
        private final BlockingQueue<XMLChunk> queue;
        private final GraphDatabaseService graphDb;
        private final Node mapperRootNode;
        private final boolean isOld;
        private XMLChunkProducer producer;

        public XMLChunkConsumer(
                BlockingQueue<XMLChunk> queue,
                GraphDatabaseService graphDb,
                Node mapperRootNode,
                boolean isOld) {
            this.queue = queue;
            this.graphDb = graphDb;
            this.mapperRootNode = mapperRootNode;
            this.isOld = isOld;
        }

        /**
         * Reset number of processed elements to 0.
         */
        public static void resetCounter() {
            count = new AtomicInteger();
        }

        @Override
        public void run() {
            int countPoisonPills = 0;
            boolean shouldRun = true;

            while (shouldRun) {
                ArrayList<XMLChunk> chunks = new ArrayList<>(SETTINGS.NR_OF_COMMIT_FEATURES);

                for (int i = 0; i < SETTINGS.NR_OF_COMMIT_FEATURES; i++) {
                    XMLChunk chunk = null;

                    try {
                        chunk = queue.take();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    if (chunk instanceof PoisonPillXMLChunk) {
                        countPoisonPills++;
                        if (countPoisonPills == SETTINGS.NR_OF_PRODUCERS) {
                            i++;
                            shouldRun = false;
                            break;
                        }

                        continue;
                    }

                    chunks.add(chunk);
                }

                try (Transaction tx = graphDb.beginTx()) {
                    int countChunk = 0;
                    for (int i = 0; i < chunks.size(); i++) {
                        XMLChunk chunk = chunks.get(i);
                        countChunk++;
                        CityGML cityObject = chunk.unmarshal();
                        Node cityObjectNode = NodeFactory.create(cityObject);
                        if (cityObject.getCityGMLClass().equals(CityGMLClass.CITY_MODEL)) {
                            mapperRootNode.createRelationshipTo(cityObjectNode,
                                    isOld ? RelationshipFactory.OLD_CITY_MODEL : RelationshipFactory.NEW_CITY_MODEL);
                            countChunk--;
                        }
                    }

                    if (countChunk > 0) {
                        logger.info((SETTINGS.SPLIT_PER_COLLECTION_MEMBER ? "Buildings" : "Features") + " found: " + count.addAndGet(countChunk));
                    }

                    tx.commit();
                } catch (IntrospectionException | UnmarshalException | MissingADESchemaException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Used as end signal.
     */
    static class PoisonPillNode implements Node {

        public PoisonPillNode() {
        }

        @Override
        public void delete() {
        }

        @Override
        public Iterable<Relationship> getRelationships() {
            return null;
        }

        @Override
        public boolean hasRelationship() {
            return false;
        }

        @Override
        public Iterable<Relationship> getRelationships(RelationshipType... types) {
            return null;
        }

        @Override
        public Iterable<Relationship> getRelationships(Direction direction, RelationshipType... types) {
            return null;
        }

        @Override
        public boolean hasRelationship(RelationshipType... types) {
            return false;
        }

        @Override
        public boolean hasRelationship(Direction direction, RelationshipType... types) {
            return false;
        }

        @Override
        public Iterable<Relationship> getRelationships(Direction dir) {
            return null;
        }

        @Override
        public boolean hasRelationship(Direction dir) {
            return false;
        }

        @Override
        public Relationship getSingleRelationship(RelationshipType type, Direction dir) {
            return null;
        }

        @Override
        public Relationship createRelationshipTo(Node otherNode, RelationshipType type) {
            return null;
        }

        @Override
        public Iterable<RelationshipType> getRelationshipTypes() {
            return null;
        }

        @Override
        public int getDegree() {
            return 0;
        }

        @Override
        public int getDegree(RelationshipType type) {
            return 0;
        }

        @Override
        public int getDegree(Direction direction) {
            return 0;
        }

        @Override
        public int getDegree(RelationshipType type, Direction direction) {
            return 0;
        }

        @Override
        public void addLabel(Label label) {

        }

        @Override
        public void removeLabel(Label label) {

        }

        @Override
        public boolean hasLabel(Label label) {
            return false;
        }

        @Override
        public Iterable<Label> getLabels() {
            return null;
        }

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public boolean hasProperty(String key) {
            return false;
        }

        @Override
        public Object getProperty(String key) {
            return null;
        }

        @Override
        public Object getProperty(String key, Object defaultValue) {
            return null;
        }

        @Override
        public void setProperty(String key, Object value) {

        }

        @Override
        public Object removeProperty(String key) {
            return null;
        }

        @Override
        public Iterable<String> getPropertyKeys() {
            return null;
        }

        @Override
        public Map<String, Object> getProperties(String... keys) {
            return null;
        }

        @Override
        public Map<String, Object> getAllProperties() {
            return null;
        }
    }

    public static class BuildingNodeProducer implements Runnable {
        private final List<Node> buildingNodes;
        private final BlockingQueue<Node> queue;

        public BuildingNodeProducer(List<Node> buildingNodes, BlockingQueue<Node> queue) {
            this.buildingNodes = buildingNodes;
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < buildingNodes.size(); i++) {
                    queue.put(buildingNodes.get(i));
                }

                // end signal by having each producer place Nconsumers pills on the queue
                // and having the consumer stop only when it receives Nproducers pills
                for (int i = 0; i < SETTINGS.CONSUMERS_PRO_PRODUCER * SETTINGS.NR_OF_PRODUCERS; i++) {
                    queue.put(new PoisonPillNode());
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static class BuildingNodeConsumer implements Runnable {
        private final static Logger logger = LoggerFactory.getLogger(BuildingNodeConsumer.class);
        private static AtomicInteger count = new AtomicInteger();
        private final BlockingQueue<Node> queue;
        private final GraphDatabaseService graphDb;
        private final Node mapperRootNode;
        private final Node matcherRootNode;
        private final Matcher matcher;
        private final EditableLayer newBuildingLayer;

        public BuildingNodeConsumer(
                BlockingQueue<Node> queue,
                GraphDatabaseService graphDb,
                Node mapperRootNode,
                Node matcherRootNode,
                Matcher matcher,
                EditableLayer newBuildingLayer) {
            this.queue = queue;
            this.graphDb = graphDb;
            this.mapperRootNode = mapperRootNode;
            this.matcherRootNode = matcherRootNode;
            this.matcher = matcher;
            this.newBuildingLayer = newBuildingLayer;
        }

        /**
         * Reset number of processed elements to 0.
         */
        public static void resetCounter() {
            count = new AtomicInteger();
        }

        @Override
        public void run() {
            int countPoisonPills = 0;
            boolean shouldRun = true;

            while (shouldRun) {
                int i = 0;

                ArrayList<Node> chunks = new ArrayList<>(SETTINGS.NR_OF_COMMIT_BUILDINGS);

                for (; i < SETTINGS.NR_OF_COMMIT_BUILDINGS; i++) {
                    Node chunk = null;
                    try {
                        chunk = queue.take();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (chunk instanceof PoisonPillNode) {
                        countPoisonPills++;
                        if (countPoisonPills == SETTINGS.NR_OF_PRODUCERS) {
                            i++;
                            shouldRun = false;
                            break;
                        }
                        continue;
                    }
                    chunks.add(chunk);
                }

                try (Transaction tx = graphDb.beginTx()) {
                    i = 0;
                    for (; i < chunks.size(); i++) {
                        Node oldBuildingNode = chunks.get(i);
                        Node newBuildingNode = GraphUtil.findBuildingInRTree(oldBuildingNode, newBuildingLayer, logger, graphDb);

                        if (newBuildingNode == null) {
                            // delete from old city model
                            matcher.createDeleteRelationshipNode(oldBuildingNode, null, matcherRootNode, false);
                        } else {
                            matcher.matchNode(oldBuildingNode, newBuildingNode, matcherRootNode, new BooleanObject(false), mapperRootNode, null);
                        }
                    }

                    if (i > 0) {
                        logger.info("Processed buildings: " + 2 * count.addAndGet(i));
                    }

                    tx.commit();
                } catch (XMLStreamException e) {
                    throw new RuntimeException(e);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}