package components;

import components.mapper.Mapper;
import conf.Bolt;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 */
public class Controller {
    // ========================
    // MAIN COMPONENTS
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);
    private final static String confFilename = "conf.json";
    private final static String confInfoFilename = "conf_info.json";

    // ========================
    // AUXILIARY
    // Embedded Neo4j Java API
    private DatabaseManagementService managementService;
    private GraphDatabaseService graphDb;
    private Path pathOld;
    private Path pathNew;

    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.init();

        // TODO Uncomment function calls

        // Map CityGML instances into a graph database in Neo4j
        controller.map();

        // Match mapped CityGML instances
        //match();

        // Export edit operations to CSV files
        //export(SETTINGS.EXPORT_LOCATION, SETTINGS.CSV_DELIMITER);

        // Execute WFS-Transactions
        //update();

        // Statistics
        //printStats();

        Bolt bolt = Project.conf.getDb().getBolt();
        if (bolt.getEnabled()) {
            logger.info("Bolt is enabled via neo4j://{}:{}", bolt.getAddress(), bolt.getPort());
            // TODO How to shutdown? Via Cypher?
            // TODO If bolt is enabled then the Docker container must persist after running
        } else {
            // Close neo4j session
            controller.shutDown();
        }
    }

    private static void setDefaultUncaughtExceptionHandler() {
        try {
            Thread.setDefaultUncaughtExceptionHandler((t, e)
                    -> logger.error("Uncaught Exception detected in thread " + t, e));
        } catch (SecurityException e) {
            logger.error("Could not set the Default Uncaught Exception Handler", e);
        }
    }

    private void init() {
        // Configurations
        try {
            // The config file must be found in `conf.json`
            Project.init(confFilename, confInfoFilename);
        } catch (IOException e) {
            logger.error("Could not read program configurations from `conf.json`\n{}", e.getMessage());
            throw new RuntimeException(e);
        }

        // Check if given folder paths exist, if not they shall be created
        String[] checkPaths = {
                Project.conf.getDb().getLocation().getNeo4jDB(),
                Project.conf.getDb().getLocation().getExportDir(),
                Project.conf.getRtree().getExportDir()
        };
        for (int i = 0; i < checkPaths.length; i++) {
            FileUtils.createFileOrDirectory(checkPaths[i], true);
        }

        pathOld = Path.of(Project.conf.getMapper().getOldFile());
        pathNew = Path.of(Project.conf.getMapper().getNewFile());

        // Clean previous database
        Path dbPath = Path.of(checkPaths[0]);
        try {
            org.neo4j.io.fs.FileUtils.deleteDirectory(dbPath);
        } catch (IOException e) {
            logger.error("Could not delete database directory {}\n{}", checkPaths[0], e.getMessage());
            throw new RuntimeException(e);
        }
        logger.info("Purged existing database in {}", checkPaths[0]);

        // Initialize a new Neo4j graph database
        managementService = (new DatabaseManagementServiceBuilder(dbPath))
                //.loadPropertiesFromFile(Paths.get(SETTINGS.NEO4JDB_CONF_LOCATION))
                .setConfig(BoltConnector.enabled, Project.conf.getDb().getBolt().getEnabled())
                .setConfig(BoltConnector.listen_address, new SocketAddress(Project.conf.getDb().getBolt().getAddress(),
                        Project.conf.getDb().getBolt().getPort()))
                .build();
        graphDb = managementService.database(DEFAULT_DATABASE_NAME);
        registerShutdownHook(managementService);
    }

    private void map() {
        Mapper mapper = new Mapper(graphDb);
        mapper.map(pathOld, pathNew);
    }

    private void registerShutdownHook(final DatabaseManagementService managementService) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread(() -> managementService.shutdown()));
    }

    private void shutDown() {
        managementService.shutdown();
        logger.info("Shut down database");
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
