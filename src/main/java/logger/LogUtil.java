package logger;

import matcher.Matcher;
import stats.Change;
import util.FileUtil;
import util.MapUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogUtil {
    private static String LINE_FORMATTER = "%-80s";
    private static String KEY_FORMATTER = "%-63s";
    private static String VALUE_FORMATTER = "%15s";

    public static Logger getLoggerWithSimpleDateFormat(String loggerName, String loggerFile) {
        // Initialize a clock for timestamps
        // df = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss ");

        return getLogger(loggerName, loggerFile, df);
    }

    public static Logger getLogger(String loggerName, String loggerFile) {
        // no formatter
        return getLogger(loggerName, loggerFile, null);
    }

    public static Logger getLogger(String loggerName, String loggerFile, SimpleDateFormat df) {
        // Initialize a logger
        Logger logger = Logger.getLogger(loggerName);
        logger.setUseParentHandlers(false);

        logger.setLevel(Level.INFO);

        FileHandler fh;
        try {
            FileUtil.createFileOrDirectory(loggerFile, false);
            fh = new FileHandler(loggerFile);
            CustomFormatter formatter = new CustomFormatter(df);
            // ConsoleHandler consoleHandler = new ConsoleHandler();
            // consoleHandler.setFormatter(formatter);
            // consoleHandler.setLevel(Level.FINEST);
            // logger.addHandler(consoleHandler);
            fh.setFormatter(formatter);

            // Logs will be shown in console and stored in a log file
            logger.addHandler(fh);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logger;
    }

    public static void logMap(Logger logger, HashMap<String, Long> occurrenceMap, String message, boolean sort) {
        StringBuilder stats = new StringBuilder();

        stats.append(getTopBorderLine());

        stats.append("\t| " + String.format(LINE_FORMATTER, message.toUpperCase().trim()) + " |\n");

        stats.append(getMapContents(occurrenceMap, sort));

        stats.append(getBottomBorderLine());

        logger.info(stats.toString());
    }

    public static void logMapWithMapValues(Logger logger, HashMap<String, HashMap<Matcher.EditOperators, Long>> occurrenceMap, String message) {
        StringBuilder stats = new StringBuilder();

        stats.append(getTopBorderLine());

        stats.append("\t| " + String.format(LINE_FORMATTER, message.toUpperCase().trim()) + " |\n");

        stats.append(getMapContentsWithMapValues(occurrenceMap, true));

        stats.append(getBottomBorderLine());

        logger.info(stats.toString());
    }

    private static String getMapContents(HashMap<String, Long> occurrenceMap, boolean sort) {
        StringBuilder stats = new StringBuilder();

        Iterator it;
        if (sort) {
            it = MapUtil.sortByValue(occurrenceMap).entrySet().iterator();
        } else {
            it = occurrenceMap.entrySet().iterator();
        }

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            stats.append(getFormattedLine(pair.getKey().toString(), (Long) pair.getValue()));
        }
        return stats.toString();
    }

    private static String getMapContentsWithMapValues(HashMap<String, HashMap<Matcher.EditOperators, Long>> occurrenceMap, boolean sort) {
        StringBuilder stats = new StringBuilder();

        Iterator i;
        if (sort) {
            i = MapUtil.sortByHashMapValue(occurrenceMap).entrySet().iterator();
        } else {
            i = occurrenceMap.entrySet().iterator();
        }

        while (i.hasNext()) {
            Map.Entry pair_i = (Map.Entry) i.next();
            // the pair.getValue() is already sorted by MapUtil.sortByHashMapValue
            Iterator j = ((HashMap<Matcher.EditOperators, Long>) pair_i.getValue()).entrySet().iterator();
            Long sum = new Long(0);
            StringBuilder subStats = new StringBuilder();
            while (j.hasNext()) {
                Map.Entry pair_j = (Map.Entry) j.next();
                Long value = (Long) pair_j.getValue();
                sum += value;
                // only show if not 0
                if (value != 0) {
                    subStats.append(getFormattedLine(" > " + pair_j.getKey().toString(), value));
                }
            }
            // only show if not 0
            if (sum != 0) {
                stats.append(getFormattedLine(pair_i.getKey().toString(), sum, true));
                stats.append(subStats);
            }
        }
        return stats.toString();
    }

    public static void logMap(Logger logger, List<String> keys, List<Long> occurrences, String message, boolean sort) {
        // assuming both the ArrayLists have the same size
        HashMap<String, Long> occurrenceMap = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            occurrenceMap.put(keys.get(i), occurrences.get(i));
        }

        logMap(logger, occurrenceMap, message, sort);
    }

    public static void logMaps(Logger logger,
                               String generalMessage,
                               List<HashMap<String, Long>> occurrenceMaps, List<String> messages,
                               boolean sort) {
        StringBuilder stats = new StringBuilder();

        stats.append(getTopBorderLine());

        stats.append("\t| " + String.format(LINE_FORMATTER, generalMessage.toUpperCase().trim()) + " |\n");

        for (int i = 0; i < occurrenceMaps.size(); i++) {
            HashMap<String, Long> occurrenceMap = occurrenceMaps.get(i);
            String message = messages.get(i);
            stats.append(getEmptyFormattedLine());
            stats.append(getDottedLine());
            stats.append(getEmptyFormattedLine());
            stats.append("\t| " + String.format(LINE_FORMATTER, message.toUpperCase().trim()) + " |\n");
            stats.append(getMapContents(occurrenceMap, sort));
        }

        stats.append(getBottomBorderLine());

        logger.info(stats.toString());
    }

    public static String getFormattedLine(String key, Number value, boolean emphasizeNumber) {
        return "\t| " + String.format(LINE_FORMATTER, "") + " |\n"
                + "\t| > " + String.format(KEY_FORMATTER, key)
                + String.format(VALUE_FORMATTER, (emphasizeNumber ? "-> " : "") + String.format("%,d", value)) + " |\n";
    }

    public static String getFormattedLine(String key, Number value) {
        return getFormattedLine(key, value, false);
    }

    public static String getFormattedLine(String message) {
        return "\t| " + String.format(LINE_FORMATTER, "") + " |\n"
                + "\t| > " + message + " |\n";
    }

    public static String getEmptyFormattedLine() {
        return "\t| " + String.format(LINE_FORMATTER, "") + " |\n";
    }

    public static void logLine(Logger logger, String key, Number value, boolean withTopBorderLine, boolean withBottomBorderLine) {
        logLine(logger, getFormattedLine(key, value), withTopBorderLine, withBottomBorderLine);
    }

    public static void logLine(Logger logger, String message, boolean withTopBorderLine, boolean withBottomBorderLine) {
        // the message must be formatted beforehand
        StringBuilder stats = new StringBuilder();
        if (withTopBorderLine) {
            stats.append(LogUtil.getTopBorderLine());
        }
        stats.append(message);
        if (withBottomBorderLine) {
            stats.append(getBottomBorderLine());
        }
        logger.info(stats.toString());
    }

    public static void logLine(Logger logger, String message) {
        logLine(logger, message, false, false);
    }

    public static void logEmptyLine(Logger logger, boolean withTopBorderLine, boolean withBottomBorderLine) {
        logLine(logger, "", withTopBorderLine, withBottomBorderLine);
    }

    public static void logEmptyLine(Logger logger) {
        logLine(logger, "", false, false);
    }

    public static Long[][] logOverviewTable(Logger logger, List<Change> rows, List<Matcher.EditOperators> cols) {
        // +-------------------+----------------+----------------+----------------+------------+------------+
        // |                   | InsertProperty | DeleteProperty | UpdateProperty | InsertNode | DeleteNode |
        // +===================+================+================+================+============+============+
        // | Thematic Changes  |                |                |                |            |            |
        // +-------------------+----------------+----------------+----------------+------------+------------+
        // | Syntactic Changes |                |                |                |            |            |
        // +-------------------+----------------+----------------+----------------+------------+------------+

        // save numbers of each cell in this table
        // the additional row / column at the end store the sums of all respective columns / rows
        // e.g. input 6 rows x 5 columns
        // output 7 rows x 6 columns, where the last row contains sums of all cells in 1st, 2nd, ... column, etc.
        Long[][] tableValues = new Long[rows.size() + 1][cols.size() + 1];

        StringBuilder stats = new StringBuilder();

        int columnWidth = 20;
        String widthFormatter = "%" + columnWidth + "s";

        // header, first cell is empty
        stats.append(getTableBorderLine(columnWidth, cols.size()));
        stats.append(String.format("%" + columnWidth + "s", ""));
        for (int i = 0; i < cols.size(); i++) {
            stats.append(String.format(widthFormatter, cols.get(i)));
        }
        stats.append("\n");

        // fill the table
        for (int i = 0; i < rows.size(); i++) {
            stats.append(getTableBorderLine(columnWidth, cols.size()));
            Change changeCategory = rows.get(i);
            stats.append(String.format(widthFormatter, changeCategory.getLabel()));
            Long sumCols = new Long(0);
            for (int j = 0; j < cols.size(); j++) {
                Matcher.EditOperators editOperator = cols.get(j);
                tableValues[i][j] = calcCell(changeCategory.getMap(), editOperator);
                stats.append(String.format(widthFormatter, String.format("%,d", tableValues[i][j])));
                sumCols += tableValues[i][j];
            }
            // save the sum of all cells in this row in the last additional column
            tableValues[i][cols.size()] = sumCols;
            // show sum of all columns in this row
            stats.append(String.format(widthFormatter, "-> " + String.format("%,d", sumCols)));
            stats.append("\n");
        }

        // show sum of all rows in each column
        stats.append(getTableBorderLine(columnWidth, cols.size()));
        stats.append(String.format(widthFormatter, ""));
        Long sumAll = new Long(0);
        for (int j = 0; j < cols.size(); j++) {
            Long sumRows = new Long(0);
            for (int i = 0; i < rows.size(); i++) {
                sumRows += tableValues[i][j];
            }
            sumAll += sumRows;
            // save the sum of all cells in this column in the last additional row
            tableValues[rows.size()][j] = sumRows;
            stats.append(String.format(widthFormatter, "-> " + String.format("%,d", sumRows)));
        }

        // save the sum of all cells in this table
        tableValues[rows.size()][cols.size()] = sumAll;
        // show sum of all values
        stats.append(String.format(widthFormatter, "-> " + String.format("%,d", sumAll)));
        stats.append("\n");
        // stats.append(getTableBorderLine(columnWidth, cols.size()));

        logger.info(stats.toString());

        return tableValues;
    }

    private static String getTableBorderLine(int columnWidth, int nrOfColumns) {
        String result = "";
        for (int i = 0; i < nrOfColumns + 1; i++) {
            result += "+";
            for (int j = 0; j < columnWidth - 1; j++) {
                result += "-";
            }
        }
        result += "+\n";

        return result;
    }

    private static Long calcCell(HashMap<String, HashMap<Matcher.EditOperators, Long>> row, Matcher.EditOperators col) {
        Long result = new Long(0);

        Iterator it = row.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String key = pair.getKey().toString();
            HashMap<Matcher.EditOperators, Long> value = (HashMap<Matcher.EditOperators, Long>) pair.getValue();
            Long numberValue = value.get(col);
            if (numberValue != null) {
                result += numberValue;
            }
        }

        return result;
    }

    private static String getTopBorderLine() {
        int width = Integer.parseInt(LINE_FORMATTER.replace("%-", "").replace("s", ""));
        String result = "\t ";
        for (int i = 0; i < width + 2; i++) {
            result += "_";
        }
        result += " \n";

        return result;
    }

    private static String getBottomBorderLine() {
        int width = Integer.parseInt(LINE_FORMATTER.replace("%-", "").replace("s", ""));
        String result = "\t\\";
        for (int i = 0; i < width + 2; i++) {
            result += "_";
        }
        result += "/\n";

        return result;
    }

    private static String getDottedLine() {
        int width = Integer.parseInt(LINE_FORMATTER.replace("%-", "").replace("s", ""));
        String result = "\t ";
        for (int i = 0; i < width + 2; i++) {
            result += ".";
        }
        result += " \n";

        return result;
    }
}
