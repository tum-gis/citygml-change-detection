package util;

import java.util.Map;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 *
 */
public class SETTINGS {

	public static final String KEY_USER_MESSAGE = "USER_MESSAGE";
	public static final String KEY_HOME_LOCATION = "HOME_LOCATION";
	public static final String KEY_TEST_DATA_LOCATION = "TEST_DATA_LOCATION";
	public static final String KEY_OLD_CITY_MODEL_LOCATION = "OLD_CITY_MODEL_LOCATION";
	public static final String KEY_NEW_CITY_MODEL_LOCATION = "NEW_CITY_MODEL_LOCATION";
	public static final String KEY_DB_LOCATION = "DB_LOCATION";
	public static final String KEY_LOG_LOCATION = "LOG_LOCATION";
	public static final String KEY_WFS_SERVER = "WFS_SERVER";
	public static final String KEY_RTREE_IMAGE_LOCATION = "RTREE_IMAGE_LOCATION";
	public static final String KEY_ENABLE_MULTI_THREADED_MAPPING = "ENABLE_MULTI_THREADED_MAPPING";
	public static final String KEY_NR_OF_PRODUCERS = "NR_OF_PRODUCERS";
	public static final String KEY_CONSUMERS_PRO_PRODUCER = "CONSUMERS_PRO_PRODUCER";
	public static final String KEY_ENABLE_INDICES = "ENABLE_INDICES";
	public static final String KEY_SPLIT_PER_COLLECTION_MEMBER = "SPLIT_PER_COLLECTION_MEMBER";
	public static final String KEY_NR_OF_COMMIT_BUILDINGS = "NR_OF_COMMIT_BUILDINGS";
	public static final String KEY_NR_OF_COMMIT_FEATURES = "NR_OF_COMMIT_FEATURES";
	public static final String KEY_NR_OF_COMMMIT_TRANS = "NR_OF_COMMMIT_TRANS";
	public static final String KEY_LOG_EVERY_N_BUILDINGS = "LOG_EVERY_N_BUILDINGS";
	public static final String KEY_MATCHING_STRATEGY = "MATCHING_STRATEGY";
	public static final String KEY_ENABLE_MULTI_THREADED_MATCHING = "ENABLE_MULTI_THREADED_MATCHING";
	public static final String KEY_MAX_RTREE_NODE_REFERENCES = "MAX_RTREE_NODE_REFERENCES";
	public static final String KEY_TILE_UNIT_X = "TILE_UNIT_X";
	public static final String KEY_TILE_UNIT_Y = "TILE_UNIT_Y";
	public static final String KEY_ERR_TOLERANCE = "ERR_TOLERANCE";
	public static final String KEY_TILE_BORDER_DISTANCE = "TILE_BORDER_DISTANCE";
	public static final String KEY_BUILDING_SHARED_VOL_PERCENTAGE_THRESHOLD = "BUILDING_SHARED_VOL_PERCENTAGE_THRESHOLD";
	public static final String KEY_CREATE_MATCHED_CONTENT_NODE = "CREATE_MATCHED_CONTENT_NODE";
	public static final String KEY_CREATE_MATCHED_GEOMETRY_NODE = "CREATE_MATCHED_GEOMETRY_NODE";
	public static final String KEY_THREAD_TIME_OUT = "THREAD_TIME_OUT";
	public static final String KEY_ENABLE_EDITORS = "ENABLE_EDITORS";
	public static final String KEY_EXECUTE_OPTIONAL = "EXECUTE_OPTIONAL";

	public static final String KEY_SETTINGS_LOCATION = "SETTINGS";
	public static final String KEY_VALUE_SEPARATOR = "=";

	public static final Map<String, String> ARGUMENTS = ReadCMDUtil.ARGUMENTS;

	/*
	 * Message for later use
	 */
	public static final String USER_MESSAGE = ARGUMENTS.get(KEY_USER_MESSAGE).toString();

	/*
	 * Database and Program settings
	 */
	public static final String HOME_LOCATION = ARGUMENTS.get(KEY_HOME_LOCATION).toString();
	// public static final String HOME_LOCATION = "C:\\Users\\Son Nguyen\\Desktop\\TestJar\\";
	// public static final String HOME_LOCATION = "/home/nguyen/GitWorkspace/CityGML2Neo4j/";

	public static final String TEST_DATA_LOCATION = HOME_LOCATION + ARGUMENTS.get(KEY_TEST_DATA_LOCATION).toString();

	public static final String OLD_CITY_MODEL_LOCATION = ARGUMENTS.get(KEY_OLD_CITY_MODEL_LOCATION) == null ? ("")
			: (TEST_DATA_LOCATION + ARGUMENTS.get(KEY_OLD_CITY_MODEL_LOCATION).toString());

	public static final String NEW_CITY_MODEL_LOCATION = ARGUMENTS.get(KEY_NEW_CITY_MODEL_LOCATION) == null ? ("")
			: TEST_DATA_LOCATION + ARGUMENTS.get(KEY_NEW_CITY_MODEL_LOCATION).toString();

	public static final String DB_LOCATION = HOME_LOCATION + ARGUMENTS.get(KEY_DB_LOCATION).toString();

	public static final String LOG_LOCATION = HOME_LOCATION + ARGUMENTS.get(KEY_LOG_LOCATION).toString();

	public static final String WFS_SERVER = ARGUMENTS.get(KEY_WFS_SERVER).toString();

	public static final String RTREE_IMAGE_LOCATION = HOME_LOCATION + ARGUMENTS.get(KEY_RTREE_IMAGE_LOCATION).toString();

	/*
	 * Mapper settings
	 */
	public static final boolean ENABLE_MULTI_THREADED_MAPPING = Boolean.parseBoolean(ARGUMENTS.get(KEY_ENABLE_MULTI_THREADED_MAPPING).toString());

	/**
	 * Number of producers in multi-threaded mapping. Between 1 and number of physical cores.
	 */
	public static final int NR_OF_PRODUCERS = Math.max(1,
			Math.min(Runtime.getRuntime().availableProcessors(),
					Integer.parseInt(ARGUMENTS.get(KEY_NR_OF_PRODUCERS).toString())));

	/**
	 * Number of consumers pro producer.
	 * 
	 * Minimum 1. Maximum (number of threads - number of producers)/(number of producers).
	 * 
	 * For instance: 2 times NR_OF_COMMIT_BUILDINGS or NR_OF_COMMIT_TRANS.
	 */
	public static final int CONSUMERS_PRO_PRODUCER = Math.max(1,
			Math.min((Runtime.getRuntime().availableProcessors() * 2 - NR_OF_PRODUCERS) / NR_OF_PRODUCERS,
					Integer.parseInt(ARGUMENTS.get(KEY_CONSUMERS_PRO_PRODUCER).toString())));
	/**
	 * Set to true to use Neo4j's indices on IDs and hrefs. This costs storage and might slow down the mapper.
	 * 
	 * Set to false to use an internal hash map to memorize nodes that have IDs and hrefs. This costs memory but is slightly faster than Neo4j indices.
	 * 
	 * Also works when multiple nodes reference to the same element (ie. have the same href).
	 */
	public static final boolean ENABLE_INDICES = Boolean.parseBoolean(ARGUMENTS.get(KEY_ENABLE_INDICES).toString());;

	/**
	 * True if only buildings should be split while importing, else all features will be split.
	 */
	public static final boolean SPLIT_PER_COLLECTION_MEMBER = Boolean.parseBoolean(ARGUMENTS.get(KEY_SPLIT_PER_COLLECTION_MEMBER).toString());;

	public static final int NR_OF_COMMIT_BUILDINGS = Integer.parseInt(ARGUMENTS.get(KEY_NR_OF_COMMIT_BUILDINGS).toString());
	/**
	 * If SPLIT_PER_COLLECTION_MEMBER is true, should be smaller than 1000 (buildings). Or else 1000 (features).
	 */
	public static final int NR_OF_COMMIT_FEATURES = SPLIT_PER_COLLECTION_MEMBER ? NR_OF_COMMIT_BUILDINGS : Integer.parseInt(ARGUMENTS.get(KEY_NR_OF_COMMIT_FEATURES).toString());

	public static final int NR_OF_COMMMIT_TRANS = Integer.parseInt(ARGUMENTS.get(KEY_NR_OF_COMMMIT_TRANS).toString());

	/**
	 * For logging purposes.
	 */
	public static final int LOG_EVERY_N_BUILDINGS = Integer.parseInt(ARGUMENTS.get(KEY_LOG_EVERY_N_BUILDINGS).toString());

	/*
	 * Matcher settings
	 */
	public enum MatchingStrategies {
		/**
		 * Distribute buildings in tiles based on their respective spatial location.
		 * 
		 * Also enable multi-threaded mode.
		 */
		TILES("TILES"),

		/**
		 * Distribute buildings in an RTree using neo4j spatial.
		 */
		RTREE("RTREE"),

		/**
		 * No strategy is applied. Also enable single-threaded mode.
		 */
		NONE("");

		private final String text;

		private MatchingStrategies(final String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public static final MatchingStrategies MATCHING_STRATEGY = MatchingStrategies.valueOf(ARGUMENTS.get(KEY_MATCHING_STRATEGY).toString());

	public static final boolean ENABLE_MULTI_THREADED_MATCHING = MATCHING_STRATEGY.equals(MatchingStrategies.TILES) ? true
			: Boolean.parseBoolean(ARGUMENTS.get(KEY_ENABLE_MULTI_THREADED_MATCHING).toString());

	/**
	 * The maximum number of entries of an internal RTree node
	 */
	public static final int MAX_RTREE_NODE_REFERENCES = Integer.parseInt(ARGUMENTS.get(KEY_MAX_RTREE_NODE_REFERENCES).toString());

	/**
	 * Width value of tiles. A tile must not be smaller than a building.
	 */
	public static final double TILE_UNIT_X = Integer.parseInt(ARGUMENTS.get(KEY_TILE_UNIT_X).toString());

	/**
	 * Height value of tiles. A tile must not be smaller than a building.
	 */
	public static final double TILE_UNIT_Y = Integer.parseInt(ARGUMENTS.get(KEY_TILE_UNIT_Y).toString());

	public static final double ERR_TOLERANCE = Double.parseDouble(ARGUMENTS.get(KEY_ERR_TOLERANCE).toString());

	/**
	 * Spatially, buildings are assigned to their respective tiles. However, if a building's bounding shape is located too near to a tile border,
	 * 
	 * it will be assigned to a new group that represents this border instead. This variable defines a threshold distance that basically considers borders
	 * 
	 * as "zones" (which makes this threshold its "radius"), any distance between a bounding shape and a border that is smaller or equal to this value is considered "too near" and thus, the building
	 * 
	 * should be moved to the border group.
	 */
	public static final double TILE_BORDER_DISTANCE = Integer.parseInt(ARGUMENTS.get(KEY_TILE_BORDER_DISTANCE).toString());

	/**
	 * To match building spatially, the ratios shared_vol/this_vol and shared_vol/other_vol must both be greater or equal this threshold.
	 */
	public static final double BUILDING_SHARED_VOL_PERCENTAGE_THRESHOLD = Double.parseDouble(ARGUMENTS.get(KEY_BUILDING_SHARED_VOL_PERCENTAGE_THRESHOLD).toString());

	/**
	 * Enable or disable auxiliary nodes that indicate two graph nodes have been successfully matched in terms of their contents.
	 * 
	 * Enabling this option will increase the total number of nodes but will reduce matching runtime.
	 */
	public static final boolean CREATE_MATCHED_CONTENT_NODE = Boolean.parseBoolean(ARGUMENTS.get(KEY_CREATE_MATCHED_CONTENT_NODE).toString());

	/**
	 * Enable or disable auxiliary nodes that indicate two graph nodes have been successfully matched in terms of their geometry.
	 * 
	 * Enabling this option will increase the total number of nodes but will reduce matching runtime.
	 */
	public static final boolean CREATE_MATCHED_GEOMETRY_NODE = Boolean.parseBoolean(ARGUMENTS.get(KEY_CREATE_MATCHED_GEOMETRY_NODE).toString());

	public static final int THREAD_TIME_OUT = Integer.parseInt(ARGUMENTS.get(KEY_THREAD_TIME_OUT).toString()); // in seconds

	/*
	 * Editor settings
	 */
	public static final boolean ENABLE_EDITORS = Boolean.parseBoolean(ARGUMENTS.get(KEY_ENABLE_EDITORS).toString());

	public static final boolean EXECUTE_OPTIONAL = Boolean.parseBoolean(ARGUMENTS.get(KEY_EXECUTE_OPTIONAL).toString());
}
