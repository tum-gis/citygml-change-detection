package stats;

import logger.LogUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private HashMap<String, Long> changedOldBuildingGmlids;

    private ThematicChange thematicChanges;
    private ProceduralChange proceduralChanges;
    private AppearanceChange appearanceChanges;
    private SyntacticChange syntacticChanges;
    private GeometricChange geometricChanges;
    private TopLevelChange topLevelChange;
    private OtherChange otherChange;

    private Logger logger;

    public StatBot(String logFolderPath, String exportCsvFolderPath, String csvDelimiter) {
        this.logFolderPath = logFolderPath;
        this.exportCsvFolderPath = exportCsvFolderPath;
        this.csvDelimiter = csvDelimiter;
        this.getAllCsvFromPath();
        this.changedOldBuildingGmlids = new HashMap<>();

        this.appearanceChanges = new AppearanceChange();
        this.geometricChanges = new GeometricChange();
        this.syntacticChanges = new SyntacticChange();
        this.proceduralChanges = new ProceduralChange();
        this.thematicChanges = new ThematicChange();
        this.topLevelChange = new TopLevelChange();
        this.otherChange = new OtherChange();

		this.logger = LogUtil.getLogger(this.getClass().toString(), "STATBOT");
    }

    public StatBot(String logFolderPath, String sheetFolderPath) {
        this(logFolderPath, sheetFolderPath, ";");
    }

    private void categorizeChanges(String propertyName, String ofNodeType) {
        // the function contains checks if this property belongs to the category
        // if true, the number of occurences of this propery shall be increased by 1
        if (this.appearanceChanges.contains(propertyName, ofNodeType)) {

        } else if (this.thematicChanges.contains(propertyName, ofNodeType)) {

        } else if (this.syntacticChanges.contains(propertyName, ofNodeType)) {
            // TODO consider also the attribute optional in the tables
        } else if (this.proceduralChanges.contains(propertyName, ofNodeType)) {

        } else if (this.topLevelChange.contains(propertyName, ofNodeType)) {

        } else if (this.geometricChanges.contains(propertyName, ofNodeType)) {

        } else {
            // if all the above categories do not meet
            // add this change to OtherChange
            this.otherChange.contains(propertyName, ofNodeType);
        }
    }

    private void printCategorizedChanges() {
        this.appearanceChanges.printMap(logger);

        this.thematicChanges.printMap(logger);

        this.proceduralChanges.printMap(logger);
    }

    private void getAllCsvFromPath() {
        this.csvDeletePropertyFiles = new ArrayList<>();
        this.csvDeleteNodeFiles = new ArrayList<>();
        this.csvInsertNodeFiles = new ArrayList<>();
        this.csvInsertPropertyFiles = new ArrayList<>();
        this.csvUpdatePropertyFiles = new ArrayList<>();

        File[] exportCsvFolders = new File(this.exportCsvFolderPath).listFiles();
        for (File exportCsvFolder : exportCsvFolders) {
            File[] exportCsvFiles = new File(exportCsvFolder.getAbsolutePath()).listFiles();
            for (File exportCsvFile : exportCsvFiles) {
                if (exportCsvFile.isFile()) {
                    if (exportCsvFile.getName().compareTo("EditOperations_DeleteProperty.csv") == 0) {
                        this.csvDeletePropertyFiles.add(exportCsvFile);
                    } else if (exportCsvFile.getName().compareTo("EditOperations_DeleteNode.csv") == 0) {
                        this.csvDeleteNodeFiles.add(exportCsvFile);
                    } else if (exportCsvFile.getName().compareTo("EditOperations_InsertNode.csv") == 0) {
                        this.csvInsertNodeFiles.add(exportCsvFile);
                    } else if (exportCsvFile.getName().compareTo("EditOperations_InsertProperty.csv") == 0) {
                        this.csvInsertPropertyFiles.add(exportCsvFile);
                    } else if (exportCsvFile.getName().compareTo("EditOperations_UpdateProperty.csv") == 0) {
                        this.csvUpdatePropertyFiles.add(exportCsvFile);
                    }
                }
            }
        }
    }

    public void printLogStats() {
        File[] logFiles = new File(this.logFolderPath).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".log");
            }
        });

        ArrayList<String> headers = new ArrayList<>();
        headers.add("Number of BUILDING nodes:");
        headers.add("Number of STRING_ATTRIBUTE nodes:");
        headers.add("Number of SURFACE_PROPERTY nodes:");
        headers.add("Number of POLYGON nodes:");
        headers.add("Number of MULTI_SURFACE nodes:");
        headers.add("Number of BUILDING_WALL_SURFACE nodes:");
        headers.add("Number of BUILDING_ROOF_SURFACE nodes:");
        headers.add("Number of BUILDING_GROUND_SURFACE nodes:");
        headers.add("Number of EXTERNAL_REFERENCE nodes:");
        headers.add("Number of SOLID nodes:");

        headers.add("TOTAL NUMBER OF CREATED NODES:");
        headers.add("MAPPER'S ELAPSED TIME:");

        headers.add("Number of DELETE_PROPERTY nodes:");
        headers.add("Number of DELETE_NODE nodes:");
        headers.add("Number of UPDATE_PROPERTY nodes:");
        headers.add("Number of INSERT_NODE nodes:");
        headers.add("Number of INSERT_PROPERTY nodes:");

        headers.add("TOTAL NUMBER OF CREATED NODES:");
        headers.add("OF WHICH ARE OPTIONAL:");
        headers.add("MATCHER'S ELAPSED TIME:");

        int indexBetweenMapperAndMatcher = 12;

        ArrayList<Long> totalNumbers = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            totalNumbers.add((long) 0);
        }

        for (File logFile : logFiles) {
            // Get total mapping time

            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(logFile));
                String line = br.readLine();

                int totalNumberOfCreatedNodesFound = 0;
                while ((line = br.readLine()) != null) {
                    for (int i = 0; i < headers.size(); i++) {
                        String header = headers.get(i);

                        if (line.indexOf(header) > 0) {
                            if (header.compareTo("TOTAL NUMBER OF CREATED NODES:") == 0) {
                                totalNumberOfCreatedNodesFound++;
                                if (totalNumberOfCreatedNodesFound == 2) {
                                    continue;
                                }
                            }

                            totalNumbers.set(i, totalNumbers.get(i) + Integer.parseInt(line.replaceAll(headers.get(i) + "|\\||seconds|nodes", "").replaceAll("\\s+", "")));
                            break;
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
        LogUtil.logMap(
                logger,
                headers.subList(0, indexBetweenMapperAndMatcher),
                totalNumbers.subList(0, indexBetweenMapperAndMatcher),
                "MAPPER");

        // MATCHER
        LogUtil.logMap(
                logger,
                headers.subList(indexBetweenMapperAndMatcher, headers.size()),
                totalNumbers.subList(indexBetweenMapperAndMatcher, headers.size()),
                "MATCHER");
    }

    public void printDeletePropertyStats() {
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

                    this.changedOldBuildingGmlids.put(propertykeys[5], null);

                    this.categorizeChanges(propertyNameString, oldNodeTypeString);
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

        LogUtil.logMaps(this.logger, "DELETE PROPERTY ...", occurrenceMaps, messages);
    }

    public void printDeleteNodeStats() {
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

                    this.changedOldBuildingGmlids.put(propertykeys[5], null);

                    this.categorizeChanges(null, deleteNodeTypeString);
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

        LogUtil.logMaps(this.logger, "DELETE NODE ...", occurrenceMaps, messages);
    }

    public void printInsertNodeStats() {
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

                    this.changedOldBuildingGmlids.put(propertykeys[6], null);

                    this.categorizeChanges(null, insertNodeTypeString);
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

        LogUtil.logMaps(this.logger, "INSERT NODE ...", occurrenceMaps, messages);
    }

    public void printInsertPropertyStats() {
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

                    this.changedOldBuildingGmlids.put(propertykeys[5], null);

                    this.categorizeChanges(propertyNameString, oldNodeTypeString);
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

        LogUtil.logMaps(this.logger, "INSERT PROPERTY ...", occurrenceMaps, messages);
    }

    public void printUpdatePropertyStats() {
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

                    this.changedOldBuildingGmlids.put(propertykeys[5], null);

                    this.categorizeChanges(propertyNameString, oldNodeTypeString);
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

        LogUtil.logMaps(this.logger, "UPDATE PROPERTY ...", occurrenceMaps, messages);
    }

    public void printSummary() {
        HashMap<String, Long> summary = new HashMap<>();

        // changed buildings
        summary.put("NUMBER OF CHANGED BUILDINGS", new Long(this.changedOldBuildingGmlids.size()));
        LogUtil.logMap(this.logger, summary, "SUMMARY");
    }

    public static void main(String[] args) {
        StatBot statBot = new StatBot("logs_tiles", "export_tiles");
        statBot.printLogStats();
        statBot.printDeletePropertyStats();
        statBot.printDeleteNodeStats();
        statBot.printInsertNodeStats();
        statBot.printInsertPropertyStats();
        statBot.printUpdatePropertyStats();

        statBot.printCategorizedChanges();
        statBot.printSummary();
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
