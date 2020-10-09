package stats;

import logger.LogUtil;
import mapper.EnumClasses;
import matcher.Matcher;
import org.citygml4j.model.citygml.CityGMLClass;
import util.FileUtil;
import util.SETTINGS;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class StatBot {

    private String logFolderPath;
    private String exportCsvFolderPath;
    private String csvDelimiter;

    private ArrayList<File> csvDeletePropertyFiles;
    private ArrayList<File> csvDeleteNodeFiles;
    private ArrayList<File> csvInsertNodeFiles;
    private ArrayList<File> csvInsertPropertyFiles;
    private ArrayList<File> csvUpdatePropertyFiles;

    private HashSet<String> changedOldBuildingGmlids;
    private HashSet<String> deletedOldBuildingGmlids;

    private Long nrOfBuildingsOld;
    private Long nrOfBuildingsNew;

    private ThematicChange thematicChanges;
    private ProceduralChange proceduralChanges;
    private SyntacticChange syntacticChanges;
    private GeometricChange geometricChanges;
    private StructuralChange structuralChange;
    private TopLevelChange topLevelChanges;
    private OtherChange otherChanges;

    private Long[][] overviewTableValues;
    private Long totalNrOfDetectedChanges;
    private Long totalNrOfRealChanges;

    private Logger logger;

    public StatBot(String logFolderPath, String exportCsvFolderPath, String csvDelimiter) {
        this.logFolderPath = logFolderPath;
        this.exportCsvFolderPath = exportCsvFolderPath;
        this.csvDelimiter = csvDelimiter;
        this.getAllCsvFromPath();
        this.changedOldBuildingGmlids = new HashSet<>();
        this.deletedOldBuildingGmlids = new HashSet<>();

        this.geometricChanges = new GeometricChange();
        this.syntacticChanges = new SyntacticChange();
        this.proceduralChanges = new ProceduralChange();
        this.thematicChanges = new ThematicChange();
        this.structuralChange = new StructuralChange();
        this.topLevelChanges = new TopLevelChange();
        this.otherChanges = new OtherChange();

        this.nrOfBuildingsOld = new Long(0);
        this.nrOfBuildingsNew = new Long(0);

        this.logger = LogUtil.getLogger(this.getClass().toString(), SETTINGS.STATBOT_OUTPUT_SUMMARY_PATH);
    }

    public StatBot(String logFolderPath, String sheetFolderPath) {
        this(logFolderPath, sheetFolderPath, ";");
    }

    public void printAllStats() {
        // this function must be called first
        this.printLogStats();

        // these must be called before the printOverviewTable() function
        // edit operators first, the categories will be listed pro edit operator
        this.printDeletePropertyStats();
        this.printDeleteNodeStats();
        this.printInsertNodeStats();
        this.printInsertPropertyStats();
        this.printUpdatePropertyStats();

        // categories first, the edit operators will be listed pro category
        this.printCategorizedChanges();

        // bring both the edit operators and categories together in one table
        this.printOverviewTable();

        // this must be called last
        this.printSummary();

        if (SETTINGS.STATBOT_OUTPUT_CSV_FOLDER != null && !SETTINGS.STATBOT_OUTPUT_CSV_FOLDER.isEmpty()) {
            this.exportCsvFiles();
        }
    }

    private boolean isRealChange(String key, Matcher.EditOperators editOperator, boolean isOptional) {
        ChangeCategories changeCategory = categorizeChanges(key, editOperator, isOptional);
        return (!changeCategory.equals(ChangeCategories.PROCEDURAL_CHANGE)
                && !changeCategory.equals(ChangeCategories.SYNTACTIC_CHANGE)
                && !changeCategory.equals(ChangeCategories.OTHER_CHANGE));
    }

    private ChangeCategories categorizeChanges(String key, Matcher.EditOperators editOperator, boolean isOptional) {
        // the function checks if this key string belongs to a change category
        // if true, the number of occurences of this key string of the edit operator shall be increased by 1

        boolean found = false;
        // if this change is optional, it is a syntactic (geometric) change
        if (isOptional) {
            // always categorize this change as syntactic change, no need for if
            this.syntacticChanges.contains(key, editOperator);
            return ChangeCategories.SYNTACTIC_CHANGE;
        }

        if (this.thematicChanges.contains(key, editOperator)) {
            return ChangeCategories.THEMATIC_CHANGE;
        } else if (this.proceduralChanges.contains(key, editOperator)) {
            return ChangeCategories.PROCEDURAL_CHANGE;
        } else if (this.structuralChange.contains(key, editOperator)) {
            return ChangeCategories.STRUCTURAL_CHANGE;
        } else if (this.topLevelChanges.contains(key, editOperator)) {
            return ChangeCategories.TOP_LEVEL_CHANGE;
        } else if (this.geometricChanges.contains(key, editOperator)) {
            // the test for geometric changes must occur after the other categorized ones
            return ChangeCategories.GEOMETRIC_CHANGE;
        } else {
            // if all the above categories do not meet
            // add this change to OtherChange
            // always categorize this change as other change, no need for if
            this.otherChanges.contains(key, editOperator);
            return ChangeCategories.OTHER_CHANGE;
        }
    }

    private void printCategorizedChanges() {
        this.proceduralChanges.logMap(logger);
        this.thematicChanges.logMap(logger);
        this.syntacticChanges.logMap(logger);
        this.geometricChanges.logMap(logger);
        this.structuralChange.logMap(logger);
        this.topLevelChanges.logMap(logger);
        this.otherChanges.logMap(logger);
    }

    private void printOverviewTable() {
        // NOTE: while changing the order of these entries,
        // the following variables MUST also be updated accordingly
        // nrOfProceduralChanges (currently 0), nrOfSyntacticChanges (currently 2)
        List<Change> rows = new ArrayList<>();
        rows.add(this.proceduralChanges);
        rows.add(this.thematicChanges);
        rows.add(this.syntacticChanges);
        rows.add(this.geometricChanges);
        rows.add(this.structuralChange);
        rows.add(this.topLevelChanges);
        rows.add(this.otherChanges);

        List<Matcher.EditOperators> cols = new ArrayList<>();
        cols.add(Matcher.EditOperators.INSERT_PROPERTY);
        cols.add(Matcher.EditOperators.DELETE_PROPERTY);
        cols.add(Matcher.EditOperators.UPDATE_PROPERTY);
        cols.add(Matcher.EditOperators.INSERT_NODE);
        cols.add(Matcher.EditOperators.DELETE_NODE);

        this.overviewTableValues = LogUtil.logOverviewTable(logger, rows, cols);
        this.totalNrOfDetectedChanges = this.overviewTableValues[rows.size()][cols.size()];
        Long nrOfProceduralChanges = this.overviewTableValues[0][cols.size()];
        Long nrOfSyntacticChanges = this.overviewTableValues[2][cols.size()];
        this.totalNrOfRealChanges = this.totalNrOfDetectedChanges
                - nrOfProceduralChanges
                - nrOfSyntacticChanges;
    }

    private void getAllCsvFromPath() {
        this.csvDeletePropertyFiles = new ArrayList<>();
        this.csvDeleteNodeFiles = new ArrayList<>();
        this.csvInsertNodeFiles = new ArrayList<>();
        this.csvInsertPropertyFiles = new ArrayList<>();
        this.csvUpdatePropertyFiles = new ArrayList<>();

        File[] exportCsvFoldersOrFiles = new File(this.exportCsvFolderPath).listFiles();
        for (File exportCsvFolderOrFile : exportCsvFoldersOrFiles) {
            if (exportCsvFolderOrFile.isDirectory()) {
                File[] exportCsvFiles = new File(exportCsvFolderOrFile.getAbsolutePath()).listFiles();
                for (File exportCsvFile : exportCsvFiles) {
                    if (exportCsvFile.isFile()) {
                        this.addToRespectiveEditOperationList(exportCsvFile);
                    }
                }
            } else {
                this.addToRespectiveEditOperationList(exportCsvFolderOrFile);
            }
        }
    }

    private void addToRespectiveEditOperationList(File file) {
        if (file.getName().compareTo("EditOperations_DeleteProperty.csv") == 0) {
            this.csvDeletePropertyFiles.add(file);
        } else if (file.getName().compareTo("EditOperations_DeleteNode.csv") == 0) {
            this.csvDeleteNodeFiles.add(file);
        } else if (file.getName().compareTo("EditOperations_InsertNode.csv") == 0) {
            this.csvInsertNodeFiles.add(file);
        } else if (file.getName().compareTo("EditOperations_InsertProperty.csv") == 0) {
            this.csvInsertPropertyFiles.add(file);
        } else if (file.getName().compareTo("EditOperations_UpdateProperty.csv") == 0) {
            this.csvUpdatePropertyFiles.add(file);
        }
    }

    private void printLogStats() {
        File[] logFiles = null;
        File logFolderOrFile = new File(this.logFolderPath);
        if (logFolderOrFile.isDirectory()) {
            logFiles = new File(this.logFolderPath).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".log");
                }
            });
        } else {
            logFiles = new File[]{logFolderOrFile};
        }

        Map<String, Long> mapperStats = new HashMap<>();
        HashMap<String, Long> matcherStats = new HashMap<>();

        for (File logFile : logFiles) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(logFile));
                String line = "";
                boolean mapperOldReached = false;
                boolean mapperNewReached = false;
                boolean statsReached = false;
                boolean statsMapperReached = false;
                boolean statsMatcherReached = false;

                String prevLine = "";
                while ((line = br.readLine()) != null) {
                    // read info from mapper
                    if (line.contains("INITIALIZING MAPPING COMPONENT FOR OLD CITY MODEL")) {
                        mapperOldReached = true;
                        mapperNewReached = false;
                    } else if (line.contains("INITIALIZING MAPPING COMPONENT FOR NEW CITY MODEL")) {
                        mapperOldReached = false;
                        mapperNewReached = true;
                        if (prevLine.contains("Buildings found:")) {
                            nrOfBuildingsOld += Long.parseLong(prevLine.split("Buildings found:")[1].trim());
                        }
                        prevLine = "";
                    } else if (line.contains("STATISTICS REPORT")) {
                        statsReached = true;
                        mapperOldReached = false;
                        mapperNewReached = false;
                        if (prevLine.contains("Buildings found:")) {
                            nrOfBuildingsNew += Long.parseLong(prevLine.split("Buildings found:")[1].trim());
                        }
                        prevLine = "";
                    }

                    if (mapperOldReached || mapperNewReached) {
                        if (line.contains("Buildings found")) {
                            prevLine = line;
                        }
                    }

                    if (line.contains("END OF LOGFILE")) {
                        break;
                    }

                    // if statistics part of the file has now been reached
                    if (statsReached) {
                        mapperOldReached = false;
                        mapperNewReached = false;

                        if (line.contains("MAPPER ...")) {
                            statsMapperReached = true;
                            statsMatcherReached = false;
                        } else if (line.contains("MATCHER ...")) {
                            statsMapperReached = false;
                            statsMatcherReached = true;
                        }

                        if (statsMapperReached) {
                            if (line.contains(":")) {
                                String[] str = line.replaceAll("\\|", "").trim().split(":");
                                String key = str[0].trim();
                                Long value = Long.parseLong(str[1].replaceAll("nodes|seconds", "").trim());
                                Long existingValue = mapperStats.get(key);
                                if (existingValue == null) {
                                    mapperStats.put(key, value);
                                } else {
                                    mapperStats.put(key, existingValue + value);
                                }
                            }
                        }

                        if (statsMatcherReached) {
                            if (line.contains(":")) {
                                String[] str = line.replaceAll("\\|", "").trim().split(":");
                                String key = str[0].trim();
                                Long value = Long.parseLong(str[1].replaceAll("nodes|seconds", "").trim());
                                Long existingValue = matcherStats.get(key);
                                if (existingValue == null) {
                                    matcherStats.put(key, value);
                                } else {
                                    matcherStats.put(key, existingValue + value);
                                }
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        this.logger.info(
                "\n\t                             STATISTICS REPORT\n" +
                        "\n\t                    brought to you by STATBOT with love\n\n");

        // MAPPER
        HashMap<String, Long> newMapperStats = new HashMap<>();
        Iterator it = mapperStats.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (!pair.getKey().toString().contains("TOTAL NUMBER OF CREATED NODES")
                    && !pair.getKey().toString().contains("MAPPER'S ELAPSED TIME")) {
                newMapperStats.put((String) pair.getKey(), (Long) pair.getValue());
            }
        }

        LogUtil.logMap(logger, newMapperStats, "MAPPER", true);

        LogUtil.logLine(logger, "TOTAL NUMBER OF CREATED NODES", mapperStats.get("TOTAL NUMBER OF CREATED NODES"), true, false);
        LogUtil.logLine(logger, "MAPPER'S ELAPSED TIME", mapperStats.get("MAPPER'S ELAPSED TIME"), false, true);

        // MATCHER
        HashMap<String, Long> newMatcherStats = new HashMap<>();
        it = matcherStats.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (!pair.getKey().toString().contains("TOTAL NUMBER OF CREATED NODES")
                    && !pair.getKey().toString().contains("OF WHICH ARE OPTIONAL")
                    && !pair.getKey().toString().contains("MATCHER'S ELAPSED TIME")) {
                newMatcherStats.put((String) pair.getKey(), (Long) pair.getValue());
            }
        }

        LogUtil.logMap(logger, newMatcherStats, "MATCHER", true);

        LogUtil.logLine(logger, "TOTAL NUMBER OF CREATED NODES", matcherStats.get("TOTAL NUMBER OF CREATED NODES"), true, false);
        LogUtil.logLine(logger, "OF WHICH ARE OPTIONAL", matcherStats.get("OF WHICH ARE OPTIONAL"), false, false);
        LogUtil.logLine(logger, "MATCHER'S ELAPSED TIME", matcherStats.get("MATCHER'S ELAPSED TIME"), false, true);
    }

    private void printDeletePropertyStats() {
        HashMap<String, Long> oldParentNodeType = new HashMap<>();
        HashMap<String, Long> propertyName = new HashMap<>();

        for (File logFile : this.csvDeletePropertyFiles) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(logFile));
                String line = br.readLine();

                while ((line = br.readLine()) != null) {
                    String[] propertykeys = line.split(this.csvDelimiter);

                    // OLD_PARENT_NODE_TYPE
                    String oldNodeTypeString = propertykeys[2];
                    Long value = oldParentNodeType.get(oldNodeTypeString);
                    oldParentNodeType.put(oldNodeTypeString, value == null ? 1 : value + 1);

                    // PROPERTY_NAME
                    String propertyNameString = propertykeys[6];
                    value = propertyName.get(propertyNameString);
                    propertyName.put(propertyNameString, value == null ? 1 : value + 1);

                    boolean isOptional = Boolean.parseBoolean(propertykeys[8]);
                    // check property name first, if it is empty then old node type
                    boolean isRealChange = false;
                    if (!propertyNameString.isEmpty()) {
                        isRealChange = this.isRealChange(propertyNameString, Matcher.EditOperators.DELETE_PROPERTY, isOptional);
                    } else {
                        isRealChange = this.isRealChange(oldNodeTypeString, Matcher.EditOperators.DELETE_PROPERTY, isOptional);
                    }
                    if (isRealChange) {
                        String ofOldBuildingId = propertykeys[5];
                        if (ofOldBuildingId != null && !ofOldBuildingId.isEmpty()) {
                            this.changedOldBuildingGmlids.add(ofOldBuildingId);
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        List<HashMap<String, Long>> occurrenceMaps = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        occurrenceMaps.add(oldParentNodeType);
        messages.add("OLD_PARENT_NODE_TYPE");

        occurrenceMaps.add(propertyName);
        messages.add("PROPERTY_NAME");

        LogUtil.logMaps(this.logger, "DELETE PROPERTY ...", occurrenceMaps, messages, true);
    }

    private void printDeleteNodeStats() {
        HashMap<String, Long> deleteNodeType = new HashMap<>();

        for (File logFile : this.csvDeleteNodeFiles) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(logFile));
                String line = br.readLine();

                while ((line = br.readLine()) != null) {
                    String[] propertykeys = line.split(this.csvDelimiter);

                    // DELETE_NODE_TYPE
                    String deleteNodeTypeString = propertykeys[2];
                    Long value = deleteNodeType.get(deleteNodeTypeString);
                    deleteNodeType.put(deleteNodeTypeString, value == null ? 1 : value + 1);

                    boolean isRealChange = false;
                    boolean isOptional = Boolean.parseBoolean(propertykeys[6]);
                    isRealChange = this.isRealChange(deleteNodeTypeString, Matcher.EditOperators.DELETE_NODE, isOptional);

                    if (isRealChange) {
                        String ofOldBuildingId = propertykeys[5];
                        if (ofOldBuildingId != null && !ofOldBuildingId.isEmpty()) {
                            if (deleteNodeTypeString.equals(CityGMLClass.BUILDING.toString())) {
                                // this is a deleted Building
                                this.deletedOldBuildingGmlids.add(ofOldBuildingId);
                            } else {
                                this.changedOldBuildingGmlids.add(ofOldBuildingId);
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        List<HashMap<String, Long>> occurrenceMaps = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        occurrenceMaps.add(deleteNodeType);
        messages.add("DELETE_NODE_TYPE");

        LogUtil.logMaps(this.logger, "DELETE NODE ...", occurrenceMaps, messages, true);
    }

    private void printInsertNodeStats() {
        HashMap<String, Long> insertRelationshipType = new HashMap<>();
        HashMap<String, Long> insertNodeType = new HashMap<>();

        for (File logFile : this.csvInsertNodeFiles) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(logFile));
                String line = br.readLine();

                while ((line = br.readLine()) != null) {
                    String[] propertykeys = line.split(this.csvDelimiter);

                    // INSERT_RELATIONSHIP_TYPE
                    String relTypeString = propertykeys[2];
                    Long value = insertRelationshipType.get(relTypeString);
                    insertRelationshipType.put(relTypeString, value == null ? 1 : value + 1);

                    // INSERT_NODE_TYPE
                    String insertNodeTypeString = propertykeys[3];
                    value = insertNodeType.get(insertNodeTypeString);
                    insertNodeType.put(insertNodeTypeString, value == null ? 1 : value + 1);

                    boolean isRealChange = false;
                    boolean isOptional = Boolean.parseBoolean(propertykeys[9]);
                    isRealChange = this.isRealChange(relTypeString, Matcher.EditOperators.INSERT_NODE, isOptional);
                    if (isRealChange) {
                        String ofOldBuildingId = propertykeys[6];
                        if (ofOldBuildingId != null && !ofOldBuildingId.isEmpty()) {
                            this.changedOldBuildingGmlids.add(ofOldBuildingId);
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        List<HashMap<String, Long>> occurrenceMaps = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        occurrenceMaps.add(insertRelationshipType);
        messages.add("INSERT_RELATIONSHIP_TYPE");

        occurrenceMaps.add(insertNodeType);
        messages.add("INSERT_NODE_TYPE");

        LogUtil.logMaps(this.logger, "INSERT NODE ...", occurrenceMaps, messages, true);
    }

    private void printInsertPropertyStats() {
        HashMap<String, Long> oldParentNodeType = new HashMap<>();
        HashMap<String, Long> propertyName = new HashMap<>();

        for (File logFile : this.csvInsertPropertyFiles) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(logFile));
                String line = br.readLine();

                while ((line = br.readLine()) != null) {
                    String[] propertykeys = line.split(this.csvDelimiter);

                    // OLD_PARENT_NODE_TYPE
                    String oldNodeTypeString = propertykeys[2];
                    Long value = oldParentNodeType.get(oldNodeTypeString);
                    oldParentNodeType.put(oldNodeTypeString, value == null ? 1 : value + 1);

                    // PROPERTY_NAME
                    String propertyNameString = propertykeys[6];
                    value = propertyName.get(propertyNameString);
                    propertyName.put(propertyNameString, value == null ? 1 : value + 1);

                    boolean isRealChange = false;
                    boolean isOptional = Boolean.parseBoolean(propertykeys[8]);
                    // check property name first, if it is empty then old node type
                    if (!propertyNameString.isEmpty()) {
                        isRealChange = this.isRealChange(propertyNameString, Matcher.EditOperators.INSERT_PROPERTY, isOptional);
                    } else {
                        isRealChange = this.isRealChange(oldNodeTypeString, Matcher.EditOperators.INSERT_PROPERTY, isOptional);
                    }
                    if (isRealChange) {
                        String ofOldBuildingId = propertykeys[5];
                        if (ofOldBuildingId != null && !ofOldBuildingId.isEmpty()) {
                            this.changedOldBuildingGmlids.add(ofOldBuildingId);
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        List<HashMap<String, Long>> occurrenceMaps = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        occurrenceMaps.add(oldParentNodeType);
        messages.add("OLD_PARENT_NODE_TYPE");

        occurrenceMaps.add(propertyName);
        messages.add("PROPERTY_NAME");

        LogUtil.logMaps(this.logger, "INSERT PROPERTY ...", occurrenceMaps, messages, true);
    }

    private void printUpdatePropertyStats() {
        HashMap<String, Long> oldParentNodeType = new HashMap<>();
        HashMap<String, Long> propertyName = new HashMap<>();

        for (File logFile : this.csvUpdatePropertyFiles) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(logFile));
                String line = br.readLine();

                while ((line = br.readLine()) != null) {
                    // concat lines that should have been a single line
                    String substring = "\"";
                    int occurrences = 0;
                    while ((occurrences = (line.length() - line.replaceAll(substring, "").length()) / substring.length()) % 2 != 0) {
                        String newLine = br.readLine();
                        if (newLine == null) {
                            break;
                        }
                        line += newLine;
                    }
                    String[] propertykeys = line.split(this.csvDelimiter);
                    if (propertykeys == null || propertykeys.length == 0 || propertykeys.length < 4) {
                        continue;
                    }

                    // OLD_PARENT_NODE_TYPE
                    String oldNodeTypeString = propertykeys[2];
                    Long value = oldParentNodeType.get(oldNodeTypeString);
                    oldParentNodeType.put(oldNodeTypeString, value == null ? 1 : value + 1);

                    // PROPERTY_NAME
                    String propertyNameString = propertykeys[6];
                    value = propertyName.get(propertyNameString);
                    propertyName.put(propertyNameString, value == null ? 1 : value + 1);

                    boolean isRealChange = false;
                    boolean isOptional = Boolean.parseBoolean(propertykeys[9]);
                    // check property name first, if it is empty then old node type
                    if (!propertyNameString.isEmpty()) {
                        isRealChange = this.isRealChange(propertyNameString, Matcher.EditOperators.UPDATE_PROPERTY, isOptional);
                    } else {
                        isRealChange = this.isRealChange(oldNodeTypeString, Matcher.EditOperators.UPDATE_PROPERTY, isOptional);
                    }
                    if (isRealChange) {
                        String ofOldBuildingId = propertykeys[5];
                        if (ofOldBuildingId != null && !ofOldBuildingId.isEmpty()) {
                            this.changedOldBuildingGmlids.add(ofOldBuildingId);
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        List<HashMap<String, Long>> occurrenceMaps = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        occurrenceMaps.add(oldParentNodeType);
        messages.add("OLD_PARENT_NODE_TYPE");

        occurrenceMaps.add(propertyName);
        messages.add("PROPERTY_NAME");

        LogUtil.logMaps(this.logger, "UPDATE PROPERTY ...", occurrenceMaps, messages, true);
    }

    private void printSummary() {
        // retain order of inserted entries
        LinkedHashMap<String, Long> summary = new LinkedHashMap<>();

        // changed buildings
        summary.put("NUMBER OF OLD BUILDINGS",
                this.nrOfBuildingsOld);
        summary.put("NUMBER OF NEW BUILDINGS",
                this.nrOfBuildingsNew);
        summary.put("NUMBER OF CHANGED OLD BUILDINGS",
                new Long(this.changedOldBuildingGmlids.size()));
        summary.put("NUMBER OF UNCHANGED OLD BUILDINGS",
                new Long(this.nrOfBuildingsOld - this.changedOldBuildingGmlids.size() - this.deletedOldBuildingGmlids.size()));
        summary.put("DELETED OLD BUILDINGS",
                new Long(this.topLevelChanges.map.get(CityGMLClass.BUILDING.toString()).get(Matcher.EditOperators.DELETE_NODE)));
        summary.put("INSERTED NEW BUILDINGS",
                new Long(this.topLevelChanges.map.get(EnumClasses.GMLRelTypes.CITY_OBJECT_MEMBER.toString()).get(Matcher.EditOperators.INSERT_NODE)));
        summary.put("TOTAL NUMBER OF DETECTED CHANGES",
                this.totalNrOfDetectedChanges);
        summary.put("TOTAL NUMBER OF REAL CHANGES",
                this.totalNrOfRealChanges);
        LogUtil.logMap(this.logger, summary, "SUMMARY", false);
    }

    private void exportCsvFiles() {
        // deleted top-level objects
        Writer writerDeleted = null;
        StringBuilder sbDeleted = new StringBuilder();
        try {
            File fDeleted = FileUtil.createFile(SETTINGS.STATBOT_OUTPUT_CSV_FOLDER + "TopLevel_Deleted.csv");
            writerDeleted = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fDeleted), "utf-8"));
            for (String gmlid : this.deletedOldBuildingGmlids) {
                sbDeleted.append(gmlid + "\n");
            }
            writerDeleted.write(sbDeleted.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writerDeleted != null) {
                try {
                    writerDeleted.close();
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
        }

        // changed top-level objects
        Writer writerChanged = null;
        StringBuilder sbChanged = new StringBuilder();
        try {
            File fChanged = FileUtil.createFile(SETTINGS.STATBOT_OUTPUT_CSV_FOLDER + "TopLevel_Changed.csv");
            writerChanged = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fChanged), "utf-8"));
            for (String gmlid : this.changedOldBuildingGmlids) {
                sbChanged.append(gmlid + "\n");
            }
            writerChanged.write(sbChanged.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writerChanged != null) {
                try {
                    writerChanged.close();
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
        }
    }

    public String getLogFolderPath() {
        return logFolderPath;
    }

    public void setLogFolderPath(String logFolderPath) {
        this.logFolderPath = logFolderPath;
    }

    public String getSheetFolderPath() {
        return exportCsvFolderPath;
    }

    public void setSheetFolderPath(String sheetFolderPath) {
        this.exportCsvFolderPath = sheetFolderPath;
    }

    public String getCsvDeletePropertyFolderPath() {
        return exportCsvFolderPath;
    }

    public void setCsvDeletePropertyFolderPath(String csvDeletePropertyFolderPath) {
        this.exportCsvFolderPath = csvDeletePropertyFolderPath;
    }

    public String getCsvDelimiter() {
        return csvDelimiter;
    }

    public void setCsvDelimiter(String csvDelimiter) {
        this.csvDelimiter = csvDelimiter;
    }

}
