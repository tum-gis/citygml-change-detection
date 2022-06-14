package components.multithreading;

import org.citygml4j.xml.io.reader.CityGMLReadException;
import org.citygml4j.xml.io.reader.CityGMLReader;
import org.citygml4j.xml.io.reader.XMLChunk;
import util.SETTINGS;

import java.util.concurrent.BlockingQueue;

public class XMLChunkProducer implements Runnable {
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
