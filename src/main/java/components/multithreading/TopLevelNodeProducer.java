package components.multithreading;

import components.Project;
import org.neo4j.graphdb.Node;

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

            // End signal by having each producer place Nconsumers pills on the queue
            // and having the consumer stop only when it receives Nproducers pills
            for (int i = 0; i < Project.conf.getMultithreading().getConsumers()
                    + Project.conf.getMultithreading().getProducers(); i++) {
                queue.put(new PoisonPillNode());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
