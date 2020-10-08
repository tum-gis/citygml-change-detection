package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import logger.LogUtil;
import org.apache.http.client.ClientProtocolException;
import org.citygml4j.CityGMLContext;
import org.citygml4j.builder.CityGMLBuilder;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.CityGMLReadException;
import org.citygml4j.xml.io.reader.CityGMLReader;
import org.citygml4j.xml.io.reader.FeatureReadMode;
import org.citygml4j.xml.io.reader.MissingADESchemaException;
import org.citygml4j.xml.io.reader.UnmarshalException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import editor.Editor;
import exporter.EditOperationExporter;
import mapper.EnumClasses.GMLRelTypes;
import mapper.Mapper;
import matcher.Matcher;
import matcher.Matcher.EditOperators;
import util.GraphUtil;
import util.MapUtil;
import util.ReadCMDUtil;
import util.SETTINGS;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 */
public class Controller {
    // Embedded Neo4j Java API
    private GraphDatabaseService graphDb;
    private Node mapperRootNode;
    private Node matcherRootNode;
    private Node editorRootNode;

    private EditOperationExporter exporter;

    private Logger logger;

    private String oldFilename;
    private String newFilename;

    private String wfsServerUrl;

    private long mapperRunTime = 0;
    private long matcherRunTime = 0;

    public Controller() {
        this.oldFilename = SETTINGS.OLD_CITY_MODEL_LOCATION;
        this.newFilename = SETTINGS.NEW_CITY_MODEL_LOCATION;
        this.wfsServerUrl = SETTINGS.WFS_SERVER;

        if (SETTINGS.CLEAN_PREVIOUS_DB) {
            // Delete all existing databases from Neo4j to begin a new session
            // this.cleanNeo4jDatabase();
            this.cleanNeo4jStorage();
        }

        // Initialize a new Neo4j graph database
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(SETTINGS.DB_LOCATION));

        // Initalize a logger
        this.logger = LogUtil.getLoggerWithSimpleDateFormat(this.getClass().toString(), SETTINGS.LOG_LOCATION);
        readSettings();
        logger.info("\n------------------------------"
                + "\nINITIALIZING TESTING COMPONENT"
                + "\n------------------------------");
    }

    // Registers a shutdown hook for the Neo4j instance so that it
    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
    // running application).
    private void registerShutdownHook() {
        // Always write database operations in transactions
        Transaction tx = graphDb.beginTx();
        try {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    graphDb.shutdown();
                }
            });
            tx.success();
        } finally {
            tx.close();
        }
    }

    private void readSettings() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n-----------------------------------------"
                + "\nREADING SETTINGS FROM " + SETTINGS.class
                + "\n-----------------------------------------");

        sb.append("\nMESSAGE OF TESTER\n");

        for (String line : SETTINGS.USER_MESSAGE.split("\n")) {
            sb.append(String.format("%-100s", "\t > " + line) + "\n");
        }

        sb.append("\nSYSTEM INFO\n");

        sb.append(String.format("%-40s", "\t > Available processors: ") + Runtime.getRuntime().availableProcessors() + " cores\n");

        sb.append(String.format("%-40s", "\t > Initial heap space size: ") + Math.round(Runtime.getRuntime().freeMemory() / (1024 * 1024 * 1024.) * 10) / 10. + " GB\n");

        // Returns the maximum amount of memory that the Java virtual machine will attempt to use.
        // If there is no inherent limit then the value Long.MAX_VALUE will be returned.
        long maxMemory = Runtime.getRuntime().maxMemory();
        sb.append(String.format("%-40s", "\t > Maximum heap space size: ") + (maxMemory == Long.MAX_VALUE ? "undefined" : Math.round(maxMemory / (1024 * 1024 * 1024.) * 10) / 10. + " GB") + "\n");

        sb.append(String.format("%-40s", "\t > Total memory available to JVM: ") + Math.round(Runtime.getRuntime().totalMemory() / (1024 * 1024 * 1024.) * 10) / 10. + " GB\n");

        sb.append("\nDATABASE AND PROGRAM SETTINGS\n");

        sb.append(String.format("%-40s", "\t > Home location:") + SETTINGS.HOME_LOCATION + "\n");

        sb.append(String.format("%-40s", "\t > Test data location:") + SETTINGS.TEST_DATA_LOCATION + "\n");

        sb.append(String.format("%-40s", "\t > Database location:") + SETTINGS.DB_LOCATION + "\n");

        sb.append(String.format("%-40s", "\t > Clean previous database:") + SETTINGS.CLEAN_PREVIOUS_DB + "\n");

        sb.append(String.format("%-40s", "\t > Log location:") + SETTINGS.LOG_LOCATION + "\n");

        sb.append(String.format("%-40s", "\t > Export location:") + SETTINGS.EXPORT_LOCATION + "\n");

        sb.append(String.format("%-40s", "\t > CSV delimiter:") + SETTINGS.CSV_DELIMITER + "\n");

        sb.append(String.format("%-40s", "\t > WFS server:") + SETTINGS.WFS_SERVER + "\n");

        // file names
        sb.append(String.format("%-40s", "\t > Old city model location:") + oldFilename + "\n");

        sb.append(String.format("%-40s", "\t > New city model location:") + newFilename + "\n");

        sb.append(String.format("%-40s", "\t > RTree image location:") + SETTINGS.RTREE_IMAGE_LOCATION + "\n");

        sb.append("\nMAPPER SETTINGS\n");

        sb.append(String.format("%-40s", "\t > Enable multi threading:") + SETTINGS.ENABLE_MULTI_THREADED_MAPPING + "\n");

        sb.append(String.format("%-40s", "\t > Number of producers:") + SETTINGS.NR_OF_PRODUCERS + "\n");

        sb.append(String.format("%-40s", "\t > Consumers per producers:") + SETTINGS.CONSUMERS_PRO_PRODUCER + "\n");

        sb.append(String.format("%-40s", "\t > Enable ID indices:") + SETTINGS.ENABLE_INDICES + "\n");

        sb.append(String.format("%-40s", "\t > Split collection member:") + SETTINGS.SPLIT_PER_COLLECTION_MEMBER + "\n");

        sb.append(String.format("%-40s", "\t > Building batch cap:") + SETTINGS.NR_OF_COMMIT_BUILDINGS + "\n");

        sb.append(String.format("%-40s", "\t > Feature batch cap:") + SETTINGS.NR_OF_COMMIT_FEATURES + "\n");

        sb.append(String.format("%-40s", "\t > Transaction batch cap:") + SETTINGS.NR_OF_COMMMIT_TRANS + "\n");

        sb.append(String.format("%-40s", "\t > Log after nr. of buildings:") + SETTINGS.LOG_EVERY_N_BUILDINGS + "\n");

        sb.append("\nMATCHER SETTINGS\n");

        sb.append(String.format("%-40s", "\t > Match only:") + SETTINGS.MATCH_ONLY + "\n");

        sb.append(String.format("%-40s", "\t > Appearance location:") + SETTINGS.APPEARANCE_LOCATION + "\n");

        sb.append(String.format("%-40s", "\t > Matching strategy:") + SETTINGS.MATCHING_STRATEGY + "\n");

        sb.append(String.format("%-40s", "\t > Enable multi threading:") + SETTINGS.ENABLE_MULTI_THREADED_MATCHING + "\n");

        sb.append(String.format("%-40s", "\t > Max. entries per RTree node:") + SETTINGS.MAX_RTREE_NODE_REFERENCES + "\n");

        sb.append(String.format("%-40s", "\t > Tile unit X:") + SETTINGS.TILE_UNIT_X + "\n");

        sb.append(String.format("%-40s", "\t > Tile unit Y:") + SETTINGS.TILE_UNIT_Y + "\n");

        sb.append(String.format("%-40s", "\t > Tile border size:") + SETTINGS.TILE_BORDER_DISTANCE + "\n");

        sb.append(String.format("%-40s", "\t > Match buildings by shared volume:") + SETTINGS.MATCH_BUILDINGS_BY_SHARED_VOLUME + "\n");

        sb.append(String.format("%-40s", "\t > Shared " + (SETTINGS.MATCH_BUILDINGS_BY_SHARED_VOLUME ? "volume" : "footprint") + " threshold" + ":") + SETTINGS.BUILDING_SHARED_VOL_PERCENTAGE_THRESHOLD * 100 + "%" + "\n");

        sb.append(String.format("%-40s", "\t > Create matched content nodes:") + SETTINGS.CREATE_MATCHED_CONTENT_NODE + "\n");

        sb.append(String.format("%-40s", "\t > Create matched geometry nodes:") + SETTINGS.CREATE_MATCHED_GEOMETRY_NODE + "\n");

        sb.append(String.format("%-40s", "\t > Thread timeout (s):") + SETTINGS.THREAD_TIME_OUT + "\n");

        sb.append(String.format("%-40s", "\t > Rounding tolerance (m):") + SETTINGS.ERR_TOLERANCE + "\n");

        sb.append(String.format("%-40s", "\t > Angle tolerance (rad):") + SETTINGS.ANGLE_TOLERANCE + "\n");

        sb.append(String.format("%-40s", "\t > Distance tolerance (m):") + SETTINGS.DISTANCE_TOLERANCE + "\n");

        sb.append("\nEDITOR SETTINGS\n");

        sb.append(String.format("%-40s", "\t > Enable Editors:") + SETTINGS.ENABLE_EDITORS + "\n");

        sb.append(String.format("%-40s", "\t > Execute optional editors:") + SETTINGS.EXECUTE_OPTIONAL + "\n");

        logger.info(sb.toString());
    }

    private void cleanNeo4jDatabase() {
        // !!! IMPORTANT: The constructor must have been executed beforehand to
        // initialize a new Neo4j and Logger session. !!!

        // Always write database operations in transactions
        Transaction tx = graphDb.beginTx();
        try {
            logger.info("... deleting existing Neo4j databases ...");
            graphDb.execute("MATCH (n) DETACH DELETE n");
            tx.success();
        } finally {
            tx.close();
        }
    }

    private void deleteRecursive(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            for (File file : fileOrDir.listFiles()) {
                deleteRecursive(file);
            }
        }

        fileOrDir.delete();
    }

    private void cleanNeo4jStorage() {
        // Remove neo4j database from the hard drive
        File neo4jDb = new File(SETTINGS.DB_LOCATION);
        this.deleteRecursive(neo4jDb);
    }

    private long calculateRunTime(long startTime, String step) {
        // Stop timestamp
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        logger.info("Step " + step + " finished");
        logger.info("Time elapsed: " + totalTime / 1000 + " seconds");

        return totalTime / 1000;
    }

    private void map() throws JAXBException, CityGMLReadException, InterruptedException, UnmarshalException, MissingADESchemaException {
        long startTime = System.currentTimeMillis();

        logger.info("... setting up citygml4j context and JAXB builder ...");
        CityGMLContext ctx = new CityGMLContext();
        CityGMLBuilder builder = ctx.createCityGMLBuilder();

        logger.info("... reading CityGML files feature by feature ...");
        CityGMLInputFactory in = builder.createCityGMLInputFactory();

        if (SETTINGS.SPLIT_PER_COLLECTION_MEMBER) {
            in.setProperty(CityGMLInputFactory.FEATURE_READ_MODE, FeatureReadMode.SPLIT_PER_COLLECTION_MEMBER);
        } else {
            in.setProperty(CityGMLInputFactory.FEATURE_READ_MODE, FeatureReadMode.SPLIT_PER_FEATURE);
        }

        in.setProperty(CityGMLInputFactory.EXCLUDE_FROM_SPLITTING, new Class[]{AbstractOpening.class, Address.class}); // to avoid unresolved xlinks
        in.setProperty(CityGMLInputFactory.KEEP_INLINE_APPEARANCE, true);

        // Initialize root node
        // Always write database operations in transactions
        Transaction tx = graphDb.beginTx();
        try {
            mapperRootNode = graphDb.createNode(Label.label("ROOT_MAPPER"));
            tx.success();
        } finally {
            tx.close();
        }

        // map old city model
        mapCityModel(in, this.oldFilename, GMLRelTypes.OLD_CITY_MODEL, true);

        // map new city model
        mapCityModel(in, this.newFilename, GMLRelTypes.NEW_CITY_MODEL, false);

        // statistics
        this.mapperRunTime = calculateRunTime(startTime, "MAPPING");
    }

    private void mapCityModel(CityGMLInputFactory in, String filename, RelationshipType relType, boolean isOld) throws CityGMLReadException, InterruptedException {
        // Always write database operations in transactions
        final Mapper mapper;
        Transaction tx = graphDb.beginTx();
        try {
            mapper = new Mapper(graphDb, logger, isOld);
            tx.success();
        } finally {
            tx.close();
        }

        if (filename.replace(SETTINGS.TEST_DATA_LOCATION, "").isEmpty()
                || filename.replace(SETTINGS.TEST_DATA_LOCATION + "/", "").isEmpty()
                || !new File(filename).exists()) {
            // if the city model does not exist -> create a dummy old/new city model node
            Transaction txx = graphDb.beginTx();
            try {
                Node targetNode = mapper.createNodeWithLabel(CityGMLClass.CITY_MODEL);
                mapperRootNode.createRelationshipTo(targetNode, relType);
                tx.success();
            } finally {
                txx.close();
            }
            return;
        }
        CityGMLReader reader = in.createCityGMLReader(new File(filename));

        mapper.mapperInit(reader, mapperRootNode);

        // due to reading large CityGML files in chunks, separate features are marked with hrefs
        // the tester simply links ROOT_MAPPER to those feature nodes with the relationship OBJECT
        // the post-processing function should then delete these OBJECT relationships
        // with the only exception is that CityModel will have an OLD__CITY_MODEL/NEW_CITY_MODEL relationship
        mapper.postProcessing(mapperRootNode, relType);

        // close reader
        reader.close();
        logger.info("Finished reading city model");
    }

    private void match() throws InterruptedException, FileNotFoundException, XMLStreamException {
        long startTime = System.currentTimeMillis();

        // initialize root node for matcher
        // Always write database operations in transactions
        Transaction tx = graphDb.beginTx();
        try {
            matcherRootNode = graphDb.createNode(Label.label("ROOT_MATCHER"));
            tx.success();
        } finally {
            tx.close();
        }

        tx = graphDb.beginTx();
        try {
            // matching without mapping again
            if (mapperRootNode == null) {
                mapperRootNode = graphDb.findNodes(Label.label("ROOT_MAPPER")).next();
            }

            tx.success();
        } finally {
            tx.close();
        }

        Matcher matcher;
        tx = graphDb.beginTx();
        try {
            matcher = new Matcher(graphDb, logger, this.oldFilename, this.newFilename);
            tx.success();
        } finally {
            tx.close();
        }

        matcher.matcherInit(mapperRootNode, matcherRootNode);
        // matcher.matchNode(
        // matcher.findFirstDescendant(mapperRootNode, GMLRelTypes.OLD_CITY_MODEL),
        // matcher.findFirstDescendant(mapperRootNode, GMLRelTypes.NEW_CITY_MODEL),
        // matcherRootNode,
        // new BooleanObject(false));

        matcher.matcherPostProcessing(matcherRootNode, graphDb);

        // statistics
        this.matcherRunTime = calculateRunTime(startTime, "MATCHING");
    }

    private void export(String exportFolder, String delimiter) {
        this.exporter = new EditOperationExporter(delimiter);
        if ((exportFolder.charAt(exportFolder.length() - 1) != '/')
                && (exportFolder.charAt(exportFolder.length() - 1) != '\\')) {
            exportFolder += "/";
        }

        String tmpLog = "Exporting created edit operations to " + exportFolder + " ...\n";
        tmpLog += this.exporter.exportEditOperationsToCSV(graphDb, matcherRootNode, exportFolder);

        logger.info(tmpLog);
    }

    private void update() throws XMLStreamException, InterruptedException, ClientProtocolException, IOException {
        // initialize root node for matcher
        // Always write database operations in transactions
        Transaction tx = graphDb.beginTx();
        try {
            editorRootNode = graphDb.createNode(Label.label("ROOT_EDITOR"));

            // updating without mapping/matching again
            if (mapperRootNode == null) {
                mapperRootNode = graphDb.findNodes(Label.label("ROOT_MAPPER")).next();
            }

            if (matcherRootNode == null) {
                matcherRootNode = graphDb.findNodes(Label.label("ROOT_MATCHER")).next();
            }

            tx.success();
        } finally {
            tx.close();
        }

        Editor editor = new Editor(graphDb, logger, oldFilename, newFilename, wfsServerUrl);

        // for demo purposes only
        // editor.executeSimpleUpdate(matcherRootNode);

        // editor.executeUpdate(mapperRootNode);
        if (SETTINGS.ENABLE_EDITORS) {
            editor.executeEditors(mapperRootNode, matcherRootNode);
        }
    }

    // Statistics
    private void printStats() {
        logger.info("Preparing statistics report ...");

        StringBuilder stats = new StringBuilder();
        stats.append("\n\t                             STATISTICS REPORT\n");

        // MAPPER
        stats.append("\t _______________________________________________________________________ \n"
                + "\t| " + String.format("%-70s", "MAPPER ...") + " |\n");
        long totalNrOfNodes = 0;

        ArrayList<Label> editorLabels = new ArrayList<>();
        for (EditOperators enu : EditOperators.values()) {
            editorLabels.add(Label.label(enu + ""));
        }

        // Iterator<Entry<String, Long>> it = MapUtil.sortByValue(CityGML2Neo4jMapper.getStats()).entrySet().iterator();
        Iterator<Entry<String, Long>> it = null;
        Transaction tx = graphDb.beginTx();
        try {
            it = MapUtil.sortByValue(GraphUtil.countAllNodesWithLabels(graphDb, tx, null, editorLabels)).entrySet().iterator();
            tx.success();
        } finally {
            tx.close();
        }

        while (it.hasNext()) {
            Map.Entry<String, Long> pair = (Map.Entry<String, Long>) it.next();
            stats.append("\t| " + String.format("%-60s", "Number of " + pair.getKey() + " nodes: ") + String.format("%10d", pair.getValue()) + " |\n");
            totalNrOfNodes += (Long) pair.getValue();
        }

        stats.append("\t| " + String.format("%-70s", "") + " |\n"
                + "\t| " + String.format("%-55s", "TOTAL NUMBER OF CREATED NODES: ") + String.format("%15s", totalNrOfNodes + " nodes") + " |\n"
                + "\t| " + String.format("%-50s", "MAPPER'S ELAPSED TIME: ") + String.format("%20s", this.mapperRunTime + " seconds") + " |\n");
        stats.append("\t ________________________________________________________________________/\n");

        // MATCHER
        if (this.exporter != null) {
            stats.append("\n\t _______________________________________________________________________ \n"
                    + "\t| " + String.format("%-70s", "MATCHER ...") + " |\n");

            totalNrOfNodes = this.exporter.getTotalNrOfEditOperations();

            // it = CityGML2Neo4jMatcher.getStats().entrySet().iterator();
            // try (Transaction tx = graphDb.beginTx()) {
            // it = GraphUtil.countAllNodesWithLabels(graphDb, tx, editorLabels,
            // null).entrySet().iterator();
            // tx.success();
            // }

            it = MapUtil.sortByValue(this.exporter.getNrOfEditOperations()).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Long> pair = (Map.Entry<String, Long>) it.next();
                stats.append("\t| " + String.format("%-60s", "Number of " + pair.getKey() + " nodes: ")
                        + String.format("%10d", pair.getValue()) + " |\n");
                // totalNrOfNodes += (Long) pair.getValue();
            }

            stats.append("\t| " + String.format("%-70s", "") + " |\n"
                    + "\t| " + String.format("%-40s", "TOTAL NUMBER OF CREATED NODES: ") + String.format("%30s", totalNrOfNodes + " nodes") + " |\n"
                    + "\t| " + String.format("%-40s", "OF WHICH ARE OPTIONAL: ") + String.format("%30s", this.exporter.getNrOfOptionalTransactions() + " nodes") + " |\n"
                    + "\t| " + String.format("%-50s", "MATCHER'S ELAPSED TIME: ") + String.format("%20s", this.matcherRunTime + " seconds") + " |\n");
            stats.append("\t ________________________________________________________________________/\n");

        }

        logger.info(stats.toString());
        logger.info("END OF LOGFILE.");
    }

    public static void main(String[] args) throws JAXBException, CityGMLReadException, InterruptedException, UnmarshalException, MissingADESchemaException, XMLStreamException, ClientProtocolException, IOException {
        // Read command line arguments if available
        ReadCMDUtil.readCommandLindArguments(args);

        Controller controller = new Controller();

        // Map CityGML instances into a graph database in Neo4j
        controller.map();

        // Match mapped CityGML instances
        controller.match();

        // Export edit operations to CSV files
        controller.export(SETTINGS.EXPORT_LOCATION, SETTINGS.CSV_DELIMITER);

        // Execute WFS-Transactions
        controller.update();

        // Statistics
        controller.printStats();

        // Close Neo4j session
        controller.registerShutdownHook();
    }
}
