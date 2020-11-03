package util;

import java.util.Map;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 */
public class SETTINGS {

    public static final String KEY_USER_MESSAGE = "USER_MESSAGE";
    public static final String KEY_HOME_LOCATION = "HOME_LOCATION";
    public static final String KEY_TEST_DATA_LOCATION = "TEST_DATA_LOCATION";
    public static final String KEY_OLD_CITY_MODEL_LOCATION = "OLD_CITY_MODEL_LOCATION";
    public static final String KEY_NEW_CITY_MODEL_LOCATION = "NEW_CITY_MODEL_LOCATION";
    public static final String KEY_DB_LOCATION = "DB_LOCATION";
    public static final String KEY_CLEAN_PREVIOUS_DB = "CLEAN_PREVIOUS_DB";
    public static final String KEY_LOG_LOCATION = "LOG_LOCATION";
    public static final String KEY_EXPORT_LOCATION = "EXPORT_LOCATION";
    public static final String KEY_CSV_DELIMITER = "CSV_DELIMITER";
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
    public static final String KEY_MATCH_ONLY = "MATCH_ONLY";
    public static final String KEY_APPEARANCE_LOCATION = "APPEARANCE_LOCATION";
    public static final String KEY_MATCHING_STRATEGY = "MATCHING_STRATEGY";
    public static final String KEY_ENABLE_MULTI_THREADED_MATCHING = "ENABLE_MULTI_THREADED_MATCHING";
    public static final String KEY_MAX_RTREE_NODE_REFERENCES = "MAX_RTREE_NODE_REFERENCES";
    public static final String KEY_TILE_UNIT_X = "TILE_UNIT_X";
    public static final String KEY_TILE_UNIT_Y = "TILE_UNIT_Y";
    public static final String KEY_ERR_TOLERANCE = "ERR_TOLERANCE";
    public static final String KEY_ANGLE_TOLERANCE = "ANGLE_TOLERANCE";
    public static final String KEY_DISTANCE_TOLERANCE = "DISTANCE_TOLERANCE";
    public static final String KEY_TILE_BORDER_DISTANCE = "TILE_BORDER_DISTANCE";
    public static final String KEY_MATCH_BUILDINGS_BY_SHARED_VOLUME = "MATCH_BUILDINGS_BY_SHARED_VOLUME";
    public static final String KEY_BUILDING_SHARED_VOL_PERCENTAGE_THRESHOLD = "BUILDING_SHARED_VOL_PERCENTAGE_THRESHOLD";
    public static final String KEY_CREATE_MATCHED_CONTENT_NODE = "CREATE_MATCHED_CONTENT_NODE";
    public static final String KEY_CREATE_MATCHED_GEOMETRY_NODE = "CREATE_MATCHED_GEOMETRY_NODE";
    public static final String KEY_THREAD_TIME_OUT = "THREAD_TIME_OUT";
    public static final String KEY_ENABLE_EDITORS = "ENABLE_EDITORS";
    public static final String KEY_EXECUTE_OPTIONAL = "EXECUTE_OPTIONAL";
    public static final String KEY_STATBOT_OUTPUT_SUMMARY_PATH = "STATBOT_OUTPUT_SUMMARY_PATH";
    public static final String KEY_STATBOT_OUTPUT_CSV_FOLDER = "STATBOT_OUTPUT_CSV_FOLDER";

    public static final String KEY_SETTINGS_LOCATION = "SETTINGS";
    public static final String KEY_VALUE_SEPARATOR = "=";

    public static final Map<String, String> ARGUMENTS = ReadCMDUtil.ARGUMENTS;

    /*
     * Message for later use
     */
    public static final String USER_MESSAGE = getValueWithDefault(KEY_USER_MESSAGE, "Default message.");

    /*
     * Database and Program settings
     */
    public static final String HOME_LOCATION = getValueWithDefault(KEY_HOME_LOCATION, "");

    public static final String OLD_CITY_MODEL_LOCATION = HOME_LOCATION + getValueWithDefault(KEY_OLD_CITY_MODEL_LOCATION, "");

    public static final String NEW_CITY_MODEL_LOCATION = HOME_LOCATION + getValueWithDefault(KEY_NEW_CITY_MODEL_LOCATION, "");

    public static final String DB_LOCATION = HOME_LOCATION + getValueWithDefault(KEY_DB_LOCATION, "neo4jDB/");

    public static final boolean CLEAN_PREVIOUS_DB = getValueWithDefault(KEY_CLEAN_PREVIOUS_DB, true);

    public static final String LOG_LOCATION = HOME_LOCATION + getValueWithDefault(KEY_LOG_LOCATION, "logs/Default.log");

    public static final String EXPORT_LOCATION = HOME_LOCATION + getValueWithDefault(KEY_EXPORT_LOCATION, "output/");

    public static final String CSV_DELIMITER = getValueWithDefault(KEY_CSV_DELIMITER, ";");

    public static final String WFS_SERVER = getValueWithDefault(KEY_WFS_SERVER, "http://localhost:8080/citydb-wfs/wfs");

    public static final String RTREE_IMAGE_LOCATION = HOME_LOCATION + getValueWithDefault(KEY_RTREE_IMAGE_LOCATION, "output/rtrees/");

    /*
     * Mapper settings
     */
    public static final boolean ENABLE_MULTI_THREADED_MAPPING = getValueWithDefault(KEY_ENABLE_MULTI_THREADED_MAPPING, true);

    /**
     * Number of producers in multi-threaded mapping. Between 1 and number of physical cores.
     */
    public static final int NR_OF_PRODUCERS = Math.max(1,
            Math.min(Runtime.getRuntime().availableProcessors(),
                    getValueWithDefault(KEY_NR_OF_PRODUCERS, 1)));

    /**
     * Number of consumers pro producer.
     * <p>
     * Minimum 1. Maximum (number of threads - number of producers)/(number of producers).
     * <p>
     * For instance: 2 times NR_OF_COMMIT_BUILDINGS or NR_OF_COMMIT_TRANS.
     */
    public static final int CONSUMERS_PRO_PRODUCER = Math.max(1,
            Math.min((Runtime.getRuntime().availableProcessors() * 2 - NR_OF_PRODUCERS) / NR_OF_PRODUCERS,
                    getValueWithDefault(KEY_CONSUMERS_PRO_PRODUCER, Runtime.getRuntime().availableProcessors() * 2 - NR_OF_PRODUCERS) / NR_OF_PRODUCERS));
    /**
     * Set to true to use Neo4j's indices on IDs and hrefs. This costs storage and might slow down the mapper.
     * <p>
     * Set to false to use an internal hash map to memorize nodes that have IDs and hrefs. This costs memory but is slightly faster than Neo4j indices.
     * <p>
     * Also works when multiple nodes reference to the same element (ie. have the same href).
     */
    public static final boolean ENABLE_INDICES = getValueWithDefault(KEY_ENABLE_INDICES, false);

    /**
     * True if only buildings should be split while importing, else all features will be split.
     */
    public static final boolean SPLIT_PER_COLLECTION_MEMBER = getValueWithDefault(KEY_SPLIT_PER_COLLECTION_MEMBER, true);

    public static final int NR_OF_COMMIT_BUILDINGS = getValueWithDefault(KEY_NR_OF_COMMIT_BUILDINGS, 10);
    /**
     * If SPLIT_PER_COLLECTION_MEMBER is true, should be smaller than 1000 (buildings). Or else 1000 (features).
     */
    public static final int NR_OF_COMMIT_FEATURES = SPLIT_PER_COLLECTION_MEMBER ? NR_OF_COMMIT_BUILDINGS : getValueWithDefault(KEY_NR_OF_COMMIT_FEATURES, 100);

    public static final int NR_OF_COMMMIT_TRANS = getValueWithDefault(KEY_NR_OF_COMMMIT_TRANS, 5000);

    /**
     * For logging purposes.
     */
    public static final int LOG_EVERY_N_BUILDINGS = getValueWithDefault(KEY_LOG_EVERY_N_BUILDINGS, 100);

    /*
     * Matcher settings
     */
    public enum MatchingStrategies {
        /**
         * Distribute buildings in tiles based on their respective spatial location.
         * <p>
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
        NONE("NONE");

        private final String text;

        private MatchingStrategies(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public static final String MATCH_ONLY = getValueWithDefault(KEY_MATCH_ONLY, "");

    public static final String APPEARANCE_LOCATION = getValueWithDefault(KEY_APPEARANCE_LOCATION, "");

    public static final MatchingStrategies MATCHING_STRATEGY = getValueWithDefault(KEY_MATCHING_STRATEGY, MatchingStrategies.RTREE);

    public static final boolean ENABLE_MULTI_THREADED_MATCHING = getValueWithDefault(KEY_ENABLE_MULTI_THREADED_MATCHING, true);

    /**
     * The maximum number of entries of an internal RTree node
     */
    public static final int MAX_RTREE_NODE_REFERENCES = getValueWithDefault(KEY_MAX_RTREE_NODE_REFERENCES, 10);

    /**
     * Width value of tiles. A tile must not be smaller than a building.
     */
    public static final double TILE_UNIT_X = getValueWithDefault(KEY_TILE_UNIT_X, 100);

    /**
     * Height value of tiles. A tile must not be smaller than a building.
     */
    public static final double TILE_UNIT_Y = getValueWithDefault(KEY_TILE_UNIT_Y, 100);

    public static final double ERR_TOLERANCE = getValueWithDefault(KEY_ERR_TOLERANCE, 1e-7);

    public static final double ANGLE_TOLERANCE = getValueWithDefault(KEY_ANGLE_TOLERANCE, 1e-3);

    public static final double DISTANCE_TOLERANCE = getValueWithDefault(KEY_DISTANCE_TOLERANCE, 1e-3);


    /**
     * Spatially, buildings are assigned to their respective tiles. However, if a building's bounding shape is located too near to a tile border,
     * <p>
     * it will be assigned to a new group that represents this border instead. This variable defines a threshold distance that basically considers borders
     * <p>
     * as "zones" (which makes this threshold its "radius"), any distance between a bounding shape and a border that is smaller or equal to this value is considered "too near" and thus, the building
     * <p>
     * should be moved to the border group.
     */
    public static final double TILE_BORDER_DISTANCE = getValueWithDefault(KEY_TILE_BORDER_DISTANCE, 5);

    /**
     * True if buildings are matched using their shared volume, otherwise only their footprints are compared.
     * If this field does not exist, the default value is true.
     */
    public static final boolean MATCH_BUILDINGS_BY_SHARED_VOLUME = getValueWithDefault(KEY_MATCH_BUILDINGS_BY_SHARED_VOLUME, true);

    /**
     * To match building spatially, the ratios shared_vol/this_vol and shared_vol/other_vol must both be greater or equal this threshold.
     */
    public static final double BUILDING_SHARED_VOL_PERCENTAGE_THRESHOLD = getValueWithDefault(KEY_BUILDING_SHARED_VOL_PERCENTAGE_THRESHOLD, 0.9);

    /**
     * Enable or disable auxiliary nodes that indicate two graph nodes have been successfully matched in terms of their contents.
     * <p>
     * Enabling this option will increase the total number of nodes but will reduce matching runtime.
     */
    public static final boolean CREATE_MATCHED_CONTENT_NODE = getValueWithDefault(KEY_CREATE_MATCHED_CONTENT_NODE, false);

    /**
     * Enable or disable auxiliary nodes that indicate two graph nodes have been successfully matched in terms of their geometry.
     * <p>
     * Enabling this option will increase the total number of nodes but will reduce matching runtime.
     */
    public static final boolean CREATE_MATCHED_GEOMETRY_NODE = getValueWithDefault(KEY_CREATE_MATCHED_GEOMETRY_NODE, false);

    public static final int THREAD_TIME_OUT = getValueWithDefault(KEY_THREAD_TIME_OUT, 500000); // in seconds

    /*
     * Editor settings
     */
    public static final boolean ENABLE_EDITORS = getValueWithDefault(KEY_ENABLE_EDITORS, false);

    public static final boolean EXECUTE_OPTIONAL = getValueWithDefault(KEY_EXECUTE_OPTIONAL, false);

    /*
     * STATBOT settings
     */
    public static final String STATBOT_OUTPUT_SUMMARY_PATH = getValueWithDefault(KEY_STATBOT_OUTPUT_SUMMARY_PATH, "STATBOT");

    public static final String STATBOT_OUTPUT_CSV_FOLDER = getValueWithDefault(KEY_STATBOT_OUTPUT_CSV_FOLDER, "export_STATBOT");

    /**
     * Auxiliary function that fetches declared configurations. If they do not exist, use default values instead.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    private static int getValueWithDefault(String key, int defaultValue) {
        if (ARGUMENTS.get(key) == null || ARGUMENTS.get(key).isEmpty()) {
            return defaultValue;
        }

        return Integer.parseInt(ARGUMENTS.get(key).toString());
    }

    /**
     * Auxiliary function that fetches declared configurations. If they do not exist, use default values instead.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    private static double getValueWithDefault(String key, double defaultValue) {
        if (ARGUMENTS.get(key) == null || ARGUMENTS.get(key).isEmpty()) {
            return defaultValue;
        }

        return Double.parseDouble(ARGUMENTS.get(key).toString());
    }

    /**
     * Auxiliary function that fetches declared configurations. If they do not exist, use default values instead.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    private static boolean getValueWithDefault(String key, boolean defaultValue) {
        if (ARGUMENTS.get(key) == null || ARGUMENTS.get(key).isEmpty()) {
            return defaultValue;
        }

        return Boolean.parseBoolean(ARGUMENTS.get(key).toString());
    }

    /**
     * Auxiliary function that fetches declared configurations. If they do not exist, use default values instead.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    private static String getValueWithDefault(String key, String defaultValue) {
        if (ARGUMENTS.get(key) == null || ARGUMENTS.get(key).isEmpty()) {
            return defaultValue;
        }

        return ARGUMENTS.get(key).toString();
    }

    /**
     * Auxiliary function that fetches declared configurations. If they do not exist, use default values instead.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    private static MatchingStrategies getValueWithDefault(String key, MatchingStrategies defaultValue) {
        if (ARGUMENTS.get(key) == null || ARGUMENTS.get(key).isEmpty()) {
            return defaultValue;
        }

        return MatchingStrategies.valueOf(ARGUMENTS.get(key).toString());
    }

    public static String readSettings() {
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

        sb.append(String.format("%-40s", "\t > Database location:") + SETTINGS.DB_LOCATION + "\n");

        sb.append(String.format("%-40s", "\t > Clean previous database:") + SETTINGS.CLEAN_PREVIOUS_DB + "\n");

        sb.append(String.format("%-40s", "\t > Log location:") + SETTINGS.LOG_LOCATION + "\n");

        sb.append(String.format("%-40s", "\t > Export location:") + SETTINGS.EXPORT_LOCATION + "\n");

        sb.append(String.format("%-40s", "\t > CSV delimiter:") + SETTINGS.CSV_DELIMITER + "\n");

        sb.append(String.format("%-40s", "\t > WFS server:") + SETTINGS.WFS_SERVER + "\n");

        // file names
        sb.append(String.format("%-40s", "\t > Old city model location:") + SETTINGS.OLD_CITY_MODEL_LOCATION + "\n");

        sb.append(String.format("%-40s", "\t > New city model location:") + SETTINGS.NEW_CITY_MODEL_LOCATION + "\n");

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

        sb.append("\nSTATBOT SETTINGS\n");

        sb.append(String.format("%-40s", "\t > Output summary:") + SETTINGS.STATBOT_OUTPUT_SUMMARY_PATH + "\n");

        sb.append(String.format("%-40s", "\t > Output summary:") + SETTINGS.STATBOT_OUTPUT_CSV_FOLDER + "\n");

        return sb.toString();
    }
}
