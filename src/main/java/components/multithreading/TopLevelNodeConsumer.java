// TODO Uncomment
/*

package components.multithreading;

import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.BooleanObject;
import util.SETTINGS;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
public class TopLevelNodeConsumer implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(TopLevelNodeConsumer.class);
    private static AtomicInteger count = new AtomicInteger();
    private final BlockingQueue<Node> queue;
    private final GraphDatabaseService graphDb;
    private final Node mapperRootNode;
    private final Node matcherRootNode;
    private final Matcher matcher;
    private final EditableLayer newTopLevelLayer;

    public TopLevelNodeConsumer(
            BlockingQueue<Node> queue,
            GraphDatabaseService graphDb,
            Node mapperRootNode,
            Node matcherRootNode,
            Matcher matcher,
            EditableLayer newTopLevelLayer) {
        this.queue = queue;
        this.graphDb = graphDb;
        this.mapperRootNode = mapperRootNode;
        this.matcherRootNode = matcherRootNode;
        this.matcher = matcher;
        this.newTopLevelLayer = newTopLevelLayer;
    }

    // Reset number of processed elements to 0.
    public static void resetCounter() {
        count = new AtomicInteger();
    }

    @Override
    public void run() {
        int countPoisonPills = 0;
        boolean shouldRun = true;

        while (shouldRun) {
            int i = 0;

            ArrayList<Node> chunks = new ArrayList<>(SETTINGS.BATCH_SIZE_TOP_LEVEL);

            for (; i < SETTINGS.BATCH_SIZE_TOP_LEVEL; i++) {
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
                    Node newBuildingNode = GraphUtil.findBuildingInRTree(oldBuildingNode, newTopLevelLayer, logger, graphDb);

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
*/
