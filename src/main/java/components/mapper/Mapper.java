package components.mapper;

import components.Project;
import components.multithreading.XMLChunkConsumer;
import components.multithreading.XMLChunkProducer;
import conf.Multithreading;
import org.citygml4j.CityGMLContext;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Timer;

import javax.xml.namespace.QName;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;

public class Mapper {
    private final static Logger logger = LoggerFactory.getLogger(Mapper.class);
    private final GraphDatabaseService graphDb;
    private final Timer timer;
    private ExecutorService service;

    public Mapper(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
        NodeFactory.createRootNodes(graphDb);
        timer = new Timer();
    }

    // Return a root node for the (acyclic directed) mapped graphs
    public void map(Path pathOld, Path pathNew) {
        timer.start();

        logger.info("Set up citygml4j context and JAXB builder");
        CityGMLContext ctx = CityGMLContext.getInstance();
        CityGMLBuilder builder = null;
        CityGMLInputFactory in = null;
        try {
            builder = ctx.createCityGMLBuilder();
            in = builder.createCityGMLInputFactory();
        } catch (CityGMLBuilderException e) {
            logger.error("Error while creating CityGML builder\n{}", e.getMessage());
            throw new RuntimeException(e);
        }

        if (Project.conf.getMultithreading().getSplitTopLevel()) {
            in.setProperty(CityGMLInputFactory.FEATURE_READ_MODE, FeatureReadMode.SPLIT_PER_COLLECTION_MEMBER);
            logger.debug("Split CityGML input datasets by top-level features");
        } else {
            in.setProperty(CityGMLInputFactory.FEATURE_READ_MODE, FeatureReadMode.SPLIT_PER_FEATURE);
            logger.debug("Split CityGML input datasets by features");
        }
        // Avoid unresolved xlinks
        in.setProperty(CityGMLInputFactory.EXCLUDE_FROM_SPLITTING,
                new QName[]{new QName(AbstractOpening.class.getName()), new QName(Address.class.getName())});
        in.setProperty(CityGMLInputFactory.KEEP_INLINE_APPEARANCE, true);

        // Global indexing for both datasets
        IndexFactory.initGlobalIndexing(graphDb);

        // Map old city model
        logger.info("Init mapping old dataset");
        mapCityModel(in, pathOld, RelationshipFactory.OLD_CITY_MODEL);
        logger.info("Mapped old dataset");

        // Map new city model
        logger.info("Init mapping new dataset");
        mapCityModel(in, pathNew, RelationshipFactory.NEW_CITY_MODEL);
        logger.info("Mapped new dataset");

        long mapperRunTime = 0;
        try {
            mapperRunTime = timer.end();
            logger.info("Mapping finished, time elapsed: {} seconds", mapperRunTime);
        } catch (Timer.TimerMissingStartException e) {
            logger.error("Error while computing run time of mapping\n{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void mapCityModel(CityGMLInputFactory in, Path path, RelationshipType relType) {
        if (path == null || !Files.exists(path)) {
            // If the city model does not exist -> create a dummy old/new city model node
            // This is useful to map only one single data set for further analyses without matching
            // TODO Refactor this
            try (Transaction tx = graphDb.beginTx()) {
                Node targetNode = tx.createNode(LabelFactory.CITY_MODEL);
                Node mapperRootNode = NodeFactory.getMapperRootNode(tx);
                mapperRootNode.createRelationshipTo(targetNode, relType);
                tx.commit();
            }
            return;
        }

        try (CityGMLReader reader = in.createCityGMLReader(path.toFile())) {
            // Pre-processing
            IndexFactory.initXLinkIndexing(graphDb);

            // Main process
            boolean isOld = (relType == RelationshipFactory.OLD_CITY_MODEL);
            if (Project.conf.getMultithreading().getEnabled()) {
                logger.info("Multi-threading is enabled");
                runMultiThreaded(reader, isOld);
            } else {
                logger.info("Multi-threading is disabled, the program shall run in single-threaded mode");
                runSingleThreaded(reader, isOld);
            }

            // Post-processing
            IndexFactory.resolveXLinks(graphDb, isOld);
            IndexFactory.dropHrefs(graphDb, isOld); // The label indexing shall be removed after resolving XLinks
            IndexFactory.initRTreeLayer(graphDb, isOld); // The spatial indexing shall remain after mapping for querying
            // TODO Other functions here

            logger.info("Finished reading city model");
        } catch (CityGMLReadException e) {
            logger.error("Error while opening the input dataset {}\n{}", path, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void runMultiThreaded(CityGMLReader reader, boolean isOld) { // TODO Test
        // Create a fixed thread pool
        int nThreads = Runtime.getRuntime().availableProcessors() * 2;
        service = Executors.newFixedThreadPool(nThreads);
        logger.debug("Set up thread pool with " + nThreads + " threads");
        // ThreadLocal<Integer> countTilCommit = new ThreadLocal<>();
        // TODO ThreadLocal variable here or in Consumer?
        // TODO Momentarily commit to database everytime a top-level feature has been mapped

        XMLChunkConsumer.resetCounter();

        Multithreading multithreading = Project.conf.getMultithreading();

        // The poison pill approach only reliably works in unbounded blocking queues
        BlockingQueue<XMLChunk> queue = new LinkedBlockingQueue<>(3
                * (multithreading.getProducers() + multithreading.getConsumers())
                * (multithreading.getSplitTopLevel() ?
                Project.conf.getBatch().getTopLevel() :
                Project.conf.getBatch().getFeature()));

        int consumersPerProducer = multithreading.getConsumers() / multithreading.getProducers();
        for (int i = 0; i < multithreading.getProducers(); i++) {
            Thread producer = new Thread(new XMLChunkProducer(reader, queue));
            service.execute(producer);
            int size = consumersPerProducer;
            if (i == multithreading.getProducers() - 1) {
                // Last loop -> assign all remaning consumers to this producer
                // (e.g. with 4 producers and 15 consumers, the first 3 producers shall have 3 consumers each
                // the last producer shall have 3 + (15 % 4) = 6 consumers)
                size = consumersPerProducer + (multithreading.getConsumers() % multithreading.getProducers());
            }
            for (int j = 0; j < size; j++) {
                Thread consumer = new Thread(new XMLChunkConsumer(queue, graphDb, isOld));
                service.execute(consumer);
            }
        }

        // Wait for all threads to finish
        service.shutdown();
        try {
            service.awaitTermination(multithreading.getTimeout(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Error while waiting for all threads to finish\n{}", e.getMessage());
            throw new RuntimeException(e);
        }
        logger.debug("Shut down thread pool");
    }

    public void runSingleThreaded(CityGMLReader reader, boolean isOld) {
        long countFeatures = 0;

        int batchSize = Project.conf.getMultithreading().getSplitTopLevel() ? Project.conf.getBatch().getTopLevel() :
                Project.conf.getBatch().getFeature();
        if (batchSize == 0) {
            logger.error("Batch size is 0, abort");
            return;
        }

        Transaction tx = graphDb.beginTx();
        try {
            while (reader.hasNext()) {
                countFeatures++;
                if (countFeatures % batchSize == 0) {
                    logger.info((Project.conf.getMultithreading().getSplitTopLevel() ? "Top-level" : "")
                            + " Features found: " + countFeatures);
                    tx.commit();
                    tx.close();
                    tx = graphDb.beginTx();
                }

                // Whereas the nextFeature() method of a CityGML reader completely unmarshals the
                // XML chunk to an instance of the citygml4j object model and optionally validates
                // it before returning, the nextChunk() method returns faster but only provides a
                // set of SAX events.
                final XMLChunk chunk = reader.nextChunk();
                CityGML cityObject;
                try {
                    cityObject = chunk.unmarshal();
                    Node node = NodeFactory.create(graphDb, cityObject, isOld);
                    if (cityObject.getCityGMLClass().equals(CityGMLClass.CITY_MODEL)) {
                        Node mapperRootNode = NodeFactory.getMapperRootNode(tx);
                        mapperRootNode.createRelationshipTo(node, RelationshipFactory.getCityModelRel(isOld));
                    }
                } catch (UnmarshalException | MissingADESchemaException | NoSuchMethodException |
                         IllegalAccessException e) {
                    logger.error("Error while reading input datasets\n{}", e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            logger.info((Project.conf.getMultithreading().getSplitTopLevel() ? "Top-level" : "")
                    + " Features found: " + countFeatures);

            tx.commit();
        } catch (CityGMLReadException e) {
            logger.error("Error while reading input datasets\n{}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            tx.close();
        }
    }

    /*
    public void postProcessing(Node mapperRootNode, RelationshipType relType) throws InterruptedException {


        // mapperTx = graphDb.beginTx();
        // try {
        // countTrans = 0;
        // cleanUpRoot(mapperRootNode, relType);
        //
        // mapperTx.success();
        // } finally {
        // mapperTx.close();
        // }


        // also assign buildings to tiles if city envelope is available, else after
        // OR also assign buildings to an RTree
        mapperTx = graphDb.beginTx();
        try {
            logger.info("Indexing buildings' BBOX in an RTree ---");
            countTrans = 0;

            calcBoundingShapes(mapperRootNode);

            if (SETTINGS.MATCHING_STRATEGY.equals(SETTINGS.MatchingStrategies.RTREE)) {
                // save image of RTree
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_");
                String dateString = dateFormat.format(new Date());

                String tmpLog = "Exporting RTree signatures as images ...\n";
                String imageName = SETTINGS.RTREE_IMAGE_LOCATION + dateString + (isOld ? "old" : "new") + "_city_model_M" + SETTINGS.MAX_RTREE_NODE_REFERENCES + ".png";

                // redirect System.out.print/ln to logger
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                PrintStream old = System.out;
                System.setOut(ps);
                GraphUtil.exportRTreeImage(buildingLayer, graphDb, imageName, logger);
                System.out.flush();
                System.setOut(old);
                String[] tmpLines = baos.toString().split("\n");
                for (int i = 0; i < tmpLines.length; i++) {
                    tmpLog += String.format("%20s", "") + tmpLines[i];
                    if (i != tmpLines.length - 1) {
                        tmpLog += "\n";
                    }
                }

                logger.info(tmpLog);
            }

            mapperTx.success();
        } finally {
            mapperTx.close();
        }
    }


    // due to reading CityGML files in chunks, separate features are marked with hrefs
    // the tester simply links ROOT_MAPPER to those feature nodes with the relationship OBJECT
    // the post-processing function should then delete these OBJECT relationships
    // with the only exception is that CityModel will have an OLD__CITY_MODEL/NEW_CITY_MODEL relationship
    private void cleanUpRoot(Node mapperRootNode, RelationshipType relType) throws InterruptedException {
        logger.info("Cleaning up temporary data ...");

        Iterable<Relationship> rels;
        for (Relationship rel : mapperRootNode.getRelationships(Direction.OUTGOING, EnumClasses.GMLRelTypes.OBJECT)) {

            countTrans++;

            if (countTrans % SETTINGS.BATCH_SIZE_TRANSACTIONS == 0) {
                // logger.info("PROCESSED FEATURES: " + countTrans);
                mapperTx.success();
                mapperTx.close();
                mapperTx = graphDb.beginTx();
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "... deleting temporary data ...");
            }

            Node otherNode = rel.getOtherNode(mapperRootNode);
            rel.delete();

            if (otherNode.hasLabel(Label.label(CityGMLClass.CITY_MODEL + ""))) {
                mapperRootNode.createRelationshipTo(otherNode, relType);
            }
        }
    }
     */
}
