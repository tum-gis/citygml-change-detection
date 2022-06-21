package components.multithreading;

import components.Project;
import components.mapper.NodeFactory;
import components.mapper.RelationshipFactory;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.xml.io.reader.MissingADESchemaException;
import org.citygml4j.xml.io.reader.UnmarshalException;
import org.citygml4j.xml.io.reader.XMLChunk;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class XMLChunkConsumer implements Runnable {
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
            int size = Project.conf.getMultithreading().getSplitTopLevel() ?
                    Project.conf.getMultithreading().getBatch().getTopLevel() :
                    Project.conf.getMultithreading().getBatch().getFeature();
            ArrayList<XMLChunk> chunks = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                XMLChunk chunk = null;

                try {
                    chunk = queue.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if (chunk instanceof PoisonPillXMLChunk) {
                    countPoisonPills++;
                    if (countPoisonPills == Project.conf.getMultithreading().getProducers()) {
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
                    logger.info((Project.conf.getMultithreading().getSplitTopLevel() ? "Top-level" : "")
                            + " Features found: " + count.addAndGet(countChunk));
                }

                tx.commit();
            } catch (UnmarshalException | MissingADESchemaException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
