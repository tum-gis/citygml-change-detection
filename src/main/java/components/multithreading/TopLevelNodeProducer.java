package components.multithreading;

import org.neo4j.graphdb.Node;
import util.SETTINGS;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class TopLevelNodeProducer implements Runnable {
    private final List<Node> topLevelNodes;
    private final BlockingQueue<Node> queue;

    public TopLevelNodeProducer(List<Node> topLevelNodes, BlockingQueue<Node> queue) {
        this.topLevelNodes = topLevelNodes;
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < topLevelNodes.size(); i++) {
                queue.put(topLevelNodes.get(i));
            }

            // end signal by having each producer place Nconsumers pills on the queue
            // and having the consumer stop only when it receives Nproducers pills
            for (int i = 0; i < SETTINGS.CONSUMERS_PRO_PRODUCER * SETTINGS.NR_OF_PRODUCERS; i++) {
                queue.put(new PoisonPillNode());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
