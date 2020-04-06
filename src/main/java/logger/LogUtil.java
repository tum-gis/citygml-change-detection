package logger;

import util.MapUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogUtil {
    private static String LINE_FORMATTER = "%-70s";
    private static String KEY_FORMATTER = "%-53s";
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

    public static void logMap(Logger logger, HashMap<String, Long> occurrenceMap, String message) {
        StringBuilder stats = new StringBuilder();

        stats.append(getTopBorderLine());

        stats.append("\t| " + String.format(LINE_FORMATTER, message.toUpperCase().trim()) + " |\n");

        stats.append(getMapContents(occurrenceMap));

        stats.append(getBottomBorderLine());

        logger.info(stats.toString());
    }

    private static String getMapContents(HashMap<String, Long> occurrenceMap) {
        StringBuilder stats = new StringBuilder();
        Iterator it = MapUtil.sortByValue(occurrenceMap).entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            stats.append(getFormattedLine(pair.getKey().toString(), (Long) pair.getValue()));
        }
        return stats.toString();
    }

    public static void logMap(Logger logger, List<String> keys, List<Long> occurrences, String message) {
        // assuming both the ArrayLists have the same size
        HashMap<String, Long> occurrenceMap = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            occurrenceMap.put(keys.get(i), occurrences.get(i));
        }

        logMap(logger, occurrenceMap, message);
    }

    public static void logMaps(Logger logger,
                               String generalMessage,
                               List<HashMap<String, Long>> occurrenceMaps, List<String> messages) {
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
            stats.append(getMapContents(occurrenceMap));
        }

        stats.append(getBottomBorderLine());

        logger.info(stats.toString());
    }

    public static String getFormattedLine(String key, Number value) {
        return "\t| " + String.format(LINE_FORMATTER, "") + " |\n"
                + "\t| > " + String.format(KEY_FORMATTER, key)
                + String.format(VALUE_FORMATTER, String.format("%,d", value)) + " |\n";
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

    private static String getTopBorderLine() {
        return "\t ________________________________________________________________________\n";
    }

    private static String getBottomBorderLine() {
        return "\t\\________________________________________________________________________/\n\n";
    }

    private static String getDottedLine() {
        return "\t|   -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  -  |\n";
    }
}
