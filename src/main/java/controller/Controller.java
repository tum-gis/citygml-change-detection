package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.http.client.ClientProtocolException;
import org.citygml4j.CityGMLContext;
import org.citygml4j.builder.CityGMLBuilder;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.CityGMLReadException;
import org.citygml4j.xml.io.reader.CityGMLReader;
import org.citygml4j.xml.io.reader.FeatureReadMode;
import org.citygml4j.xml.io.reader.MissingADESchemaException;
import org.citygml4j.xml.io.reader.UnmarshalException;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import editor.Editor;
import mapper.EnumClasses.GMLRelTypes;
import mapper.Mapper;
import matcher.Matcher;
import matcher.Matcher.DeletePropertyNodeProperties;
import matcher.Matcher.DeleteRelationshipNodeProperties;
import matcher.Matcher.EditOperators;
import matcher.Matcher.EditRelTypes;
import matcher.Matcher.InsertPropertyNodeProperties;
import matcher.Matcher.InsertRelationshipNodeProperties;
import matcher.Matcher.UpdatePropertyNodeProperties;
import util.GraphUtil;
import util.MapUtil;
import util.ReadCMDUtil;
import util.SETTINGS;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 *
 */
public class Controller {
	// Embedded Neo4j Java API
	private GraphDatabaseService graphDb;
	private Node mapperRootNode;
	private Node matcherRootNode;
	private Node editorRootNode;

	private Logger logger;
	private SimpleDateFormat df;

	private String oldFilename;
	private String newFilename;

	private String wfsServerUrl;

	private long mapperRunTime = 0;
	private long matcherRunTime = 0;

	private StringBuilder createdEditors;

	public Controller(String oldFilename, String newFilename) {
		// Initialize a new Neo4j session with default input
		this(SETTINGS.DB_LOCATION, oldFilename, newFilename, SETTINGS.WFS_SERVER);
	}

	public Controller(String dbPathName, String oldFilename, String newFilename, String wfsServerUrl) {
		this.oldFilename = oldFilename;
		this.newFilename = newFilename;
		this.wfsServerUrl = wfsServerUrl;

		// Initialize a new Neo4j graph database
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(dbPathName));

		// Initialize a clock for timestamps
		// df = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ");
		df = new SimpleDateFormat("HH:mm:ss ");

		// Initialize a logger
		this.logger = Logger.getLogger(Mapper.class.getName());
		logger.setUseParentHandlers(false);

		logger.setLevel(Level.INFO);

		FileHandler fh;
		try {
			fh = new FileHandler(SETTINGS.LOG_LOCATION);
			CustomFormatter formatter = new CustomFormatter();
			// ConsoleHandler consoleHandler = new ConsoleHandler();
			// consoleHandler.setFormatter(formatter);
			// consoleHandler.setLevel(Level.FINEST);
			// logger.addHandler(consoleHandler);

			fh.setFormatter(formatter);
			logger.addHandler(fh);
			// Logs will be shown in console and stored in a log file
			logger.info("BEGIN OF LOGFILE FOR " + this.getClass().toString());

			readSettings();

			logger.info("\n------------------------------"
					+ "\nINITIALIZING TESTING COMPONENT"
					+ "\n------------------------------");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Registers a shutdown hook for the Neo4j instance so that it
	// shuts down nicely when the VM exits (even if you "Ctrl-C" the
	// running application).
	private void registerShutdownHook() {
		// Always write database operations in transactions
		try (Transaction tx = graphDb.beginTx()) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					graphDb.shutdown();
				}
			});
			tx.success();
		}
	}

	private class CustomFormatter extends Formatter {
		@Override

		public String format(LogRecord record) {
			StringBuilder builder = new StringBuilder(1000);
			builder.append("[" + df.format(new Date()) + " ");
			builder.append(String.format("%8s", Thread.currentThread().getName().replace("pool-", "p").replace("thread-", "t") + "]") + " ");
			// builder.append("[").append(record.getSourceClassName()).append(".");
			// builder.append(record.getSourceMethodName()).append("] ");
			// builder.append("[").append(record.getLevel()).append("] ");
			builder.append(formatMessage(record));
			builder.append("\n");

			// Print on the console
			System.out.print(builder.toString());

			return builder.toString();
		}

		@Override
		public String getHead(Handler h) {
			return super.getHead(h);
		}

		@Override
		public String getTail(Handler h) {
			return super.getTail(h);
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

		sb.append(String.format("%-40s", "\t > Log location") + SETTINGS.LOG_LOCATION + "\n");

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

		sb.append(String.format("%-40s", "\t > Rounding tolerance:") + SETTINGS.ERR_TOLERANCE + "\n");

		sb.append("\nEDITOR SETTINGS\n");

		sb.append(String.format("%-40s", "\t > Enable Editors:") + SETTINGS.ENABLE_EDITORS + "\n");

		sb.append(String.format("%-40s", "\t > Execute optional editors:") + SETTINGS.EXECUTE_OPTIONAL + "\n");

		logger.info(sb.toString());
	}

	private void cleanNeo4jDatabase() {
		// !!! IMPORTANT: The constructor must have been executed beforehand to
		// initialize a new Neo4j and Logger session. !!!

		// Always write database operations in transactions
		try (Transaction tx = graphDb.beginTx()) {
			logger.info("... deleting existing Neo4j databases ...");
			graphDb.execute("MATCH (n) DETACH DELETE n");
			tx.success();
		}
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

		in.setProperty(CityGMLInputFactory.EXCLUDE_FROM_SPLITTING, new Class[] { AbstractOpening.class, Address.class }); // to avoid unresolved xlinks
		in.setProperty(CityGMLInputFactory.KEEP_INLINE_APPEARANCE, true);

		// Initialize root node
		// Always write database operations in transactions
		try (Transaction tx = graphDb.beginTx()) {
			mapperRootNode = graphDb.createNode(Label.label("ROOT_MAPPER"));
			tx.success();
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
		try (Transaction tx = graphDb.beginTx()) {
			mapper = new Mapper(graphDb, logger, isOld);
			tx.success();
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
		try (Transaction tx = graphDb.beginTx()) {
			matcherRootNode = graphDb.createNode(Label.label("ROOT_MATCHER"));
			tx.success();
		}

		try (Transaction tx = graphDb.beginTx()) {
			// matching without mapping again
			if (mapperRootNode == null) {
				mapperRootNode = graphDb.findNodes(Label.label("ROOT_MAPPER")).next();
			}

			tx.success();
		}

		Matcher matcher;
		try (Transaction tx = graphDb.beginTx()) {
			matcher = new Matcher(graphDb, logger, this.oldFilename, this.newFilename);
			tx.success();
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

		// edit operators
		int count = 1;
		this.createdEditors = new StringBuilder();
		this.createdEditors.append("\n\t                          CREATED EDIT OPERATORS\n");
		this.createdEditors.append("\t           n.* : optional transaction for geometrically equivalent\n");
		this.createdEditors.append("\t                   objects with different representations\n");
		this.createdEditors.append("\t ------------------------------------------------------------------------ \n");
		try (Transaction tx = graphDb.beginTx()) {
			for (Iterator<Node> nodeIt = GraphUtil.findChildrenOfNode(matcherRootNode, EditRelTypes.CONSISTS_OF).iterator(); nodeIt.hasNext();) {
				Node node = (Node) nodeIt.next();
				this.createdEditors.append("\t" + String.format("%2d", (count++)) + ".");
				this.createdEditors.append((node.getProperty("isOptional").toString().equals("true") ? "* " : "  "));
				this.createdEditors.append(node.getLabels().toString() + "\n");

				String[] properties = null;
				if (node.hasLabel(Label.label(EditOperators.UPDATE_PROPERTY + ""))) {
					properties = new String[] {
							UpdatePropertyNodeProperties.PROPERTY_NAME.toString(),
							UpdatePropertyNodeProperties.OLD_VALUE.toString(),
							UpdatePropertyNodeProperties.NEW_VALUE.toString(),
							UpdatePropertyNodeProperties.IS_GENERIC.toString(),
							UpdatePropertyNodeProperties.MESSAGE.toString(),
							"",
							UpdatePropertyNodeProperties.OLD_NEAREST_ID.toString(),
							UpdatePropertyNodeProperties.OLD_BUILDING_ID.toString(),
					};
				} else if (node.hasLabel(Label.label(EditOperators.DELETE_PROPERTY + ""))) {
					properties = new String[] {
							DeletePropertyNodeProperties.PROPERTY_NAME.toString(),
							DeletePropertyNodeProperties.MESSAGE.toString(),
							"",
							DeletePropertyNodeProperties.OLD_NEAREST_ID.toString(),
							DeletePropertyNodeProperties.OLD_BUILDING_ID.toString(),
					};
				} else if (node.hasLabel(Label.label(EditOperators.INSERT_PROPERTY + ""))) {
					properties = new String[] {
							InsertPropertyNodeProperties.PROPERTY_NAME.toString(),
							InsertPropertyNodeProperties.NEW_VALUE.toString(),
							InsertPropertyNodeProperties.MESSAGE.toString(),
							"",
							InsertPropertyNodeProperties.OLD_NEAREST_ID.toString(),
							InsertPropertyNodeProperties.OLD_BUILDING_ID.toString(),
					};
				} else if (node.hasLabel(Label.label(EditOperators.DELETE_NODE + ""))) {
					properties = new String[] {
							DeleteRelationshipNodeProperties.IS_GENERIC.toString(),
							DeleteRelationshipNodeProperties.MESSAGE.toString(),
							"",
							DeleteRelationshipNodeProperties.OLD_NEAREST_ID.toString(),
							DeleteRelationshipNodeProperties.OLD_BUILDING_ID.toString(),
					};
				} else if (node.hasLabel(Label.label(EditOperators.INSERT_NODE + ""))) {
					properties = new String[] {
							InsertRelationshipNodeProperties.RELATIONSHIP_TYPE.toString(),
							InsertRelationshipNodeProperties.IS_GENERIC.toString(),
							InsertRelationshipNodeProperties.MESSAGE.toString(),
							"",
							InsertRelationshipNodeProperties.OLD_NEAREST_ID.toString(),
							InsertRelationshipNodeProperties.NEW_NEAREST_ID.toString(),
							InsertRelationshipNodeProperties.OLD_BUILDING_ID.toString(),
							InsertRelationshipNodeProperties.NEW_BUILDING_ID.toString(),
					};
				}

				for (String key : properties) {
					this.createdEditors.append(key.contentEquals("") ? "\n" : "\t\t > " + key + " = " + node.getProperty(key) + "\n");
				}

				this.createdEditors.append("\n");

				for (RelationshipType relType : EditRelTypes.values()) {
					if (node.hasRelationship(Direction.OUTGOING, relType)) {
						this.createdEditors.append("\t\t -- " + relType + " --> ");
						Node otherNode = node.getSingleRelationship(relType, Direction.OUTGOING).getOtherNode(node);
						this.createdEditors.append(otherNode.getLabels().toString() + "\n");

						Iterator<Entry<String, Object>> it = otherNode.getAllProperties().entrySet().iterator();
						while (it.hasNext()) {
							Map.Entry<String, Object> pair = (Map.Entry<String, Object>) it.next();
							this.createdEditors.append("\t\t\t\t > " + pair.getKey() + " = " + pair.getValue() + "\n");
						}
					}
				}

				this.createdEditors.append("\t\t -------------------------------------------------------- \n");
			}
			tx.success();
		}
	}

	private void update() throws XMLStreamException, InterruptedException, ClientProtocolException, IOException {
		// initialize root node for matcher
		// Always write database operations in transactions
		try (Transaction tx = graphDb.beginTx()) {
			editorRootNode = graphDb.createNode(Label.label("ROOT_EDITOR"));

			// updating without mapping/matching again
			if (mapperRootNode == null) {
				mapperRootNode = graphDb.findNodes(Label.label("ROOT_MAPPER")).next();
			}

			if (matcherRootNode == null) {
				matcherRootNode = graphDb.findNodes(Label.label("ROOT_MATCHER")).next();
			}

			tx.success();
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
			tx.success();
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
		stats.append("\n\t _______________________________________________________________________ \n"
				+ "\t| " + String.format("%-70s", "MATCHER ...") + " |\n");
		totalNrOfNodes = 0;

		// it = CityGML2Neo4jMatcher.getStats().entrySet().iterator();
		try (Transaction tx = graphDb.beginTx()) {
			it = GraphUtil.countAllNodesWithLabels(graphDb, tx, editorLabels, null).entrySet().iterator();
			tx.success();
		}

		while (it.hasNext()) {
			Map.Entry<String, Long> pair = (Map.Entry<String, Long>) it.next();
			stats.append("\t| " + String.format("%-60s", "Number of " + pair.getKey() + " nodes: ") + String.format("%10d", pair.getValue()) + " |\n");
			totalNrOfNodes += (Long) pair.getValue();
		}

		stats.append("\t| " + String.format("%-70s", "") + " |\n"
				+ "\t| " + String.format("%-40s", "TOTAL NUMBER OF CREATED NODES: ") + String.format("%30s", totalNrOfNodes + " nodes") + " |\n"
				+ "\t| " + String.format("%-40s", "OF WHICH ARE OPTIONAL: ") + String.format("%30s", Matcher.getNrOfOptionalTransactions() + "/" + totalNrOfNodes + " nodes") + " |\n"
				+ "\t| " + String.format("%-50s", "MATCHER'S ELAPSED TIME: ") + String.format("%20s", this.matcherRunTime + " seconds") + " |\n");
		stats.append("\t ________________________________________________________________________/\n");

		stats.append("\n" + this.createdEditors);

		logger.info(stats.toString());
		logger.info("END OF LOGFILE.");
	}

	public static void main(String[] args) throws JAXBException, CityGMLReadException, InterruptedException, UnmarshalException, MissingADESchemaException, XMLStreamException, ClientProtocolException, IOException {
		/**
		 * Read command line arguments if available
		 */
		ReadCMDUtil.readCommandLindArguments(args);

		Controller controller;

		if (SETTINGS.OLD_CITY_MODEL_LOCATION == null || SETTINGS.OLD_CITY_MODEL_LOCATION.isEmpty()
				|| SETTINGS.NEW_CITY_MODEL_LOCATION == null || SETTINGS.NEW_CITY_MODEL_LOCATION.isEmpty()) {

			controller = new Controller(
					SETTINGS.TEST_DATA_LOCATION + "BerlinMoabitETRS89Old.gml",
					SETTINGS.TEST_DATA_LOCATION + "BerlinMoabitETRS89New.gml");
		} else {
			controller = new Controller(
					SETTINGS.OLD_CITY_MODEL_LOCATION,
					SETTINGS.NEW_CITY_MODEL_LOCATION);
		}

		/*
		 * Delete all existing databases from Neo4j to begin a new session
		 */
		controller.cleanNeo4jDatabase();

		/*
		 * Map CityGML instances into a graph database in Neo4j
		 */
		controller.map();

		/*
		 * Match mapped CityGML instances
		 */
		controller.match();

		/*
		 * Execute WFS-Transactions
		 */
		controller.update();

		/*
		 * Statistics
		 */
		controller.printStats();

		/*
		 * Close Neo4j session
		 */
		controller.registerShutdownHook();
	}
}
