package components;

import components.mapper.Mapper;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.io.fs.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FileUtil;
import util.ReadCMDUtil;
import util.SETTINGS;

import java.io.IOException;
import java.nio.file.Paths;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 */
public class Controller {
    // ========================
    // MAIN COMPONENTS
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);
    // TODO mapper, matcher, etc.
    private final long matcherRunTime = 0;
    // ========================
    // AUXILIARY
    // Embedded Neo4j Java API
    private DatabaseManagementService managementService;
    private GraphDatabaseService graphDb;
    private Node mapperRootNode;
    private Node matcherRootNode;
    private Node editorRootNode;
    private String oldFilename;
    private String newFilename;
    private String wfsServerUrl;

    public Controller(String configFile) {
        // Read command line arguments if available
        try {
            if (configFile == null || !ReadCMDUtil.readCommandLindArguments(configFile)) {
                ReadCMDUtil.readCommandLindArguments("config/Default.txt");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        init();
    }

    public Controller(String[] args) {
        // Read command line arguments if available
        try {
            if (args == null || !ReadCMDUtil.readCommandLindArguments(args)) {
                ReadCMDUtil.readCommandLindArguments("config/Default.txt");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        init();
    }

    public static void main(String[] args) {
        Controller controller = new Controller(args);
        // Controller controller = new Controller("config/Default.txt");

        controller.execute();
    }

    private void init() {
        // Check if given folder paths exist, if not they shall be created
        String[] checkPaths = {
                SETTINGS.DB_LOCATION,
                SETTINGS.EXPORT_LOCATION,
                SETTINGS.RTREE_IMAGE_LOCATION
        };
        for (int i = 0; i < checkPaths.length; i++) {
            FileUtil.createFileOrDirectory(checkPaths[i], true);
        }

        oldFilename = SETTINGS.OLD_CITY_MODEL_LOCATION;
        newFilename = SETTINGS.NEW_CITY_MODEL_LOCATION;
        wfsServerUrl = SETTINGS.WFS_SERVER;

        if (SETTINGS.CLEAN_PREVIOUS_DB) {
            try {
                FileUtils.deleteDirectory(Paths.get(SETTINGS.DB_LOCATION));
            } catch (IOException e) {
                logger.error("Could not delete database directory {}\n{}", SETTINGS.DB_LOCATION, e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            // Delete all existing databases from Neo4j to begin a new session
            cleanNeo4jDatabase();
        }

        // Initialize a new Neo4j graph database
        managementService = (new DatabaseManagementServiceBuilder(Paths.get(SETTINGS.DB_LOCATION)))
                //.loadPropertiesFromFile(Paths.get(SETTINGS.NEO4JDB_CONF_LOCATION))
                .build();
        graphDb = managementService.database(DEFAULT_DATABASE_NAME);
        registerShutdownHook(managementService);

        // Initalize logger
        // TODO SETTINGS.LOG_LOCATION
        logger.info(SETTINGS.readSettings());
        logger.info("\n------------------------------"
                + "\nINITIALIZING TESTING COMPONENT"
                + "\n------------------------------");
    }

    public void execute() {
        // TODO Uncomment function calls
        // Map CityGML instances into a graph database in Neo4j
        Mapper mapper = new Mapper(graphDb);
        mapper.map(oldFilename, newFilename);

        // Match mapped CityGML instances
        //match();
        // Export edit operations to CSV files
        //export(SETTINGS.EXPORT_LOCATION, SETTINGS.CSV_DELIMITER);
        // Execute WFS-Transactions
        //update();
        // Statistics
        //printStats();
        // Close Neo4j session

        shutDown();
    }

    private void registerShutdownHook(final DatabaseManagementService managementService) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                managementService.shutdown();
            }
        });
    }

    private void shutDown() {
        managementService.shutdown();
        logger.info("Shut down database");
    }

    private void cleanNeo4jDatabase() {
        logger.info("... deleting existing Neo4j databases ...");
        try (Transaction tx = graphDb.beginTx();
             Result result = tx.execute("MATCH (n) DETACH DELETE n")) {
            System.out.println(result.resultAsString()); // TODO
        }
    }

    // TODO Uncomment
/*
    private void match() throws InterruptedException, FileNotFoundException, XMLStreamException {
        long startTime = System.currentTimeMillis();

        // initialize root node for matcher
        try (Transaction tx = graphDb.beginTx()) {
            matcherRootNode = tx.createNode(Label.label("ROOT_MATCHER"));
            tx.commit();
        }

        try (Transaction tx = graphDb.beginTx()) {
            // matching without mapping again
            if (mapperRootNode == null) {
                mapperRootNode = tx.findNodes(Label.label("ROOT_MAPPER")).next();
            }
            tx.commit();
        }

        Matcher matcher;
        try (Transaction tx = graphDb.beginTx()) {
            matcher = new Matcher(graphDb, logger, oldFilename, newFilename);
            tx.commit();
        }

        matcher.matcherInit(mapperRootNode, matcherRootNode);
        // matcher.matchNode(
        // matcher.findFirstDescendant(mapperRootNode, GMLRelTypes.OLD_CITY_MODEL),
        // matcher.findFirstDescendant(mapperRootNode, GMLRelTypes.NEW_CITY_MODEL),
        // matcherRootNode,
        // new BooleanObject(false));

        matcher.matcherPostProcessing(matcherRootNode, graphDb);

        // statistics
        matcherRunTime = calculateRunTime(startTime, "MATCHING");
    }

    private void export(String exportFolder, String delimiter) {
        exporter = new EditOperationExporter(delimiter);
        if ((exportFolder.charAt(exportFolder.length() - 1) != '/')
                && (exportFolder.charAt(exportFolder.length() - 1) != '\\')) {
            exportFolder += "/";
        }

        String tmpLog = "Exporting created edit operations to " + exportFolder + " ...\n";
        tmpLog += exporter.exportEditOperationsToCSV(graphDb, matcherRootNode, exportFolder);

        logger.info(tmpLog);
    }

    private void update() throws XMLStreamException, InterruptedException, IOException {
        // initialize root node for matcher
        try (Transaction tx = graphDb.beginTx()) {
            editorRootNode = tx.createNode(Label.label("ROOT_EDITOR"));

            // updating without mapping/matching again
            if (mapperRootNode == null) {
                mapperRootNode = tx.findNodes(Label.label("ROOT_MAPPER")).next();
            }

            if (matcherRootNode == null) {
                matcherRootNode = tx.findNodes(Label.label("ROOT_MATCHER")).next();
            }

            tx.commit();
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
        try (Transaction tx = graphDb.beginTx()) {
            it = MapUtil.sortByValue(GraphUtil.countAllNodesWithLabels(graphDb, tx, null, editorLabels)).entrySet().iterator();
            tx.commit();
        }

        while (it.hasNext()) {
            Map.Entry<String, Long> pair = it.next();
            stats.append("\t| " + String.format("%-60s", "Number of " + pair.getKey() + " nodes: ") + String.format("%10d", pair.getValue()) + " |\n");
            totalNrOfNodes += pair.getValue();
        }

        stats.append("\t| " + String.format("%-70s", "") + " |\n"
                + "\t| " + String.format("%-55s", "TOTAL NUMBER OF CREATED NODES: ") + String.format("%15s", totalNrOfNodes + " nodes") + " |\n"
                + "\t| " + String.format("%-50s", "MAPPER'S ELAPSED TIME: ") + String.format("%20s", mapperRunTime + " seconds") + " |\n");
        stats.append("\t ________________________________________________________________________/\n");

        // MATCHER
        if (exporter != null) {
            stats.append("\n\t _______________________________________________________________________ \n"
                    + "\t| " + String.format("%-70s", "MATCHER ...") + " |\n");

            totalNrOfNodes = exporter.getTotalNrOfEditOperations();

            // it = CityGML2Neo4jMatcher.getStats().entrySet().iterator();
            // try (Transaction tx = graphDb.beginTx()) {
            // it = GraphUtil.countAllNodesWithLabels(graphDb, tx, editorLabels,
            // null).entrySet().iterator();
            // tx.success();
            // }

            it = MapUtil.sortByValue(exporter.getNrOfEditOperations()).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Long> pair = it.next();
                stats.append("\t| " + String.format("%-60s", "Number of " + pair.getKey() + " nodes: ")
                        + String.format("%10d", pair.getValue()) + " |\n");
                // totalNrOfNodes += (Long) pair.getValue();
            }

            stats.append("\t| " + String.format("%-70s", "") + " |\n"
                    + "\t| " + String.format("%-40s", "TOTAL NUMBER OF CREATED NODES: ") + String.format("%30s", totalNrOfNodes + " nodes") + " |\n"
                    + "\t| " + String.format("%-40s", "OF WHICH ARE OPTIONAL: ") + String.format("%30s", exporter.getNrOfOptionalTransactions() + " nodes") + " |\n"
                    + "\t| " + String.format("%-50s", "MATCHER'S ELAPSED TIME: ") + String.format("%20s", matcherRunTime + " seconds") + " |\n");
            stats.append("\t ________________________________________________________________________/\n");

        }

        logger.info(stats.toString());
        logger.info("END OF LOGFILE.");

        // Statistics
        statBot = new StatBot(SETTINGS.LOG_LOCATION, SETTINGS.EXPORT_LOCATION, SETTINGS.CSV_DELIMITER);
        statBot.printAllStats();
    }
 */
}
