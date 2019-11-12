package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StatBot {

	private String logFolderPath;
	private String exportCsvFolderPath;
	private String csvDelimiter;
	
	private ArrayList<File> csvDeletePropertyFiles;
	private ArrayList<File> csvDeleteNodeFiles;
	private ArrayList<File> csvInsertNodeFiles;
	private ArrayList<File> csvInsertPropertyFiles;
	private ArrayList<File> csvUpdatePropertyFiles;

	public StatBot(String logFolderPath, String exportCsvFolderPath, String csvDelimiter) {
		this.logFolderPath = logFolderPath;
		this.exportCsvFolderPath = exportCsvFolderPath;
		this.csvDelimiter = csvDelimiter;
		this.getAllCsvFromPath();
	}

	public StatBot(String logFolderPath, String sheetFolderPath) {
		this(logFolderPath, sheetFolderPath, ";");
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
		
		StringBuilder stats = new StringBuilder();
		stats.append("\n\t                             STATISTICS REPORT\n");
		stats.append("\n\t                    brought to you by StatBot with love\n\n");

		// MAPPER
		stats.append("\t _______________________________________________________________________ \n"
				+ "\t| " + String.format("%-70s", "MAPPER ...") + " |\n");
		for (int i = 0; i < indexBetweenMapperAndMatcher; i++) {
			stats.append("\t| " + String.format("%-70s", "") + " |\n"
					+ "\t| " + String.format("%-55s", headers.get(i)) + String.format("%15s", totalNumbers.get(i)) + " |\n");
		}
		stats.append("\t ________________________________________________________________________/\n\n");
		
		// MATCHER
		stats.append("\t _______________________________________________________________________ \n"
				+ "\t| " + String.format("%-70s", "MATCHER ...") + " |\n");
		for (int i = indexBetweenMapperAndMatcher; i < headers.size(); i++) {
			stats.append("\t| " + String.format("%-70s", "") + " |\n"
					+ "\t| " + String.format("%-55s", headers.get(i)) + String.format("%15s", totalNumbers.get(i)) + " |\n");
		}
		stats.append("\t ________________________________________________________________________/\n");
		
		System.out.println(stats);
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
					String key = propertykeys[2];
					Long value = oldParentNodeType.get(key);
					oldParentNodeType.put(key, value == null ? 1 : value + 1);
					
					// PROPERTY_NAME
					key = propertykeys[6];
					value = propertyName.get(key);
					propertyName.put(key, value == null ? 1 : value + 1);
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
		
		StringBuilder stats = new StringBuilder();
		stats.append("\t _______________________________________________________________________ \n"
				+ "\t| " + String.format("%-70s", "DELETE PROPERTY ...") + " |\n");
		
		// OLD_PARENT_NODE_TYPE
		stats.append("\t| " + String.format("%-70s", "") + " |\n");
		stats.append("\t| " + String.format("%-70s", "OLD_PARENT_NODE_TYPE") + " |\n");
		Iterator it = MapUtil.sortByValue(oldParentNodeType).entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        stats.append("\t| " + String.format("%-70s", "") + " |\n"
					+ "\t| > " + String.format("%-53s", pair.getKey()) + String.format("%15s", pair.getValue()) + " |\n");
	    }
	    
	    // PROPERTY_NAME
	    stats.append("\t| " + String.format("%-70s", "") + " |\n");
 		stats.append("\t| " + String.format("%-70s", "PROPERTY_NAME") + " |\n");
 		it = MapUtil.sortByValue(propertyName).entrySet().iterator();
 	    while (it.hasNext()) {
 	        Map.Entry pair = (Map.Entry)it.next();
 	        stats.append("\t| " + String.format("%-70s", "") + " |\n"
 					+ "\t| > " + String.format("%-53s", pair.getKey()) + String.format("%15s", pair.getValue()) + " |\n");
 	    }
	 	    
		stats.append("\t ________________________________________________________________________/\n\n");
		
		System.out.println(stats);
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
					String key = propertykeys[2];
					Long value = deleteNodeType.get(key);
					deleteNodeType.put(key, value == null ? 1 : value + 1);
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
		
		StringBuilder stats = new StringBuilder();
		stats.append("\t _______________________________________________________________________ \n"
				+ "\t| " + String.format("%-70s", "DELETE NODE ...") + " |\n");
		
		// DELETE_NODE_TYPE
		stats.append("\t| " + String.format("%-70s", "") + " |\n");
		stats.append("\t| " + String.format("%-70s", "DELETE_NODE_TYPE") + " |\n");
		Iterator it = MapUtil.sortByValue(deleteNodeType).entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        stats.append("\t| " + String.format("%-70s", "") + " |\n"
					+ "\t| > " + String.format("%-53s", pair.getKey()) + String.format("%15s", pair.getValue()) + " |\n");
	    }
	 	    
		stats.append("\t ________________________________________________________________________/\n\n");
		
		System.out.println(stats);
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
					String key = propertykeys[2];
					Long value = insertRelationshipType.get(key);
					insertRelationshipType.put(key, value == null ? 1 : value + 1);
					
					// INSERT_NODE_TYPE
					key = propertykeys[3];
					value = insertNodeType.get(key);
					insertNodeType.put(key, value == null ? 1 : value + 1);
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
		
		StringBuilder stats = new StringBuilder();
		stats.append("\t _______________________________________________________________________ \n"
				+ "\t| " + String.format("%-70s", "INSERT NODE ...") + " |\n");
		
		// INSERT_RELATIONSHIP_TYPE
		stats.append("\t| " + String.format("%-70s", "") + " |\n");
		stats.append("\t| " + String.format("%-70s", "INSERT_RELATIONSHIP_TYPE") + " |\n");
		Iterator it = MapUtil.sortByValue(insertRelationshipType).entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        stats.append("\t| " + String.format("%-70s", "") + " |\n"
					+ "\t| > " + String.format("%-53s", pair.getKey()) + String.format("%15s", pair.getValue()) + " |\n");
	    }
	    
	    // INSERT_NODE_TYPE
	    stats.append("\t| " + String.format("%-70s", "") + " |\n");
 		stats.append("\t| " + String.format("%-70s", "INSERT_NODE_TYPE") + " |\n");
 		it = MapUtil.sortByValue(insertNodeType).entrySet().iterator();
 	    while (it.hasNext()) {
 	        Map.Entry pair = (Map.Entry)it.next();
 	        stats.append("\t| " + String.format("%-70s", "") + " |\n"
 					+ "\t| > " + String.format("%-53s", pair.getKey()) + String.format("%15s", pair.getValue()) + " |\n");
 	    }
	 	    
		stats.append("\t ________________________________________________________________________/\n\n");
		
		System.out.println(stats);
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
					String key = propertykeys[2];
					Long value = oldParentNodeType.get(key);
					oldParentNodeType.put(key, value == null ? 1 : value + 1);
					
					// PROPERTY_NAME
					key = propertykeys[6];
					value = propertyName.get(key);
					propertyName.put(key, value == null ? 1 : value + 1);
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
		
		StringBuilder stats = new StringBuilder();
		stats.append("\t _______________________________________________________________________ \n"
				+ "\t| " + String.format("%-70s", "INSERT PROPERTY ...") + " |\n");
		
		// OLD_PARENT_NODE_TYPE
		stats.append("\t| " + String.format("%-70s", "") + " |\n");
		stats.append("\t| " + String.format("%-70s", "OLD_PARENT_NODE_TYPE") + " |\n");
		Iterator it = MapUtil.sortByValue(oldParentNodeType).entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        stats.append("\t| " + String.format("%-70s", "") + " |\n"
					+ "\t| > " + String.format("%-53s", pair.getKey()) + String.format("%15s", pair.getValue()) + " |\n");
	    }
	    
	    // PROPERTY_NAME
	    stats.append("\t| " + String.format("%-70s", "") + " |\n");
 		stats.append("\t| " + String.format("%-70s", "PROPERTY_NAME") + " |\n");
 		it = MapUtil.sortByValue(propertyName).entrySet().iterator();
 	    while (it.hasNext()) {
 	        Map.Entry pair = (Map.Entry)it.next();
 	        stats.append("\t| " + String.format("%-70s", "") + " |\n"
 					+ "\t| > " + String.format("%-53s", pair.getKey()) + String.format("%15s", pair.getValue()) + " |\n");
 	    }
	 	    
		stats.append("\t ________________________________________________________________________/\n\n");
		
		System.out.println(stats);
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
					String[] propertykeys = line.split(this.csvDelimiter);
					if (propertykeys == null || propertykeys.length == 0 || propertykeys.length < 4) {
						continue;
					}
					
					// OLD_PARENT_NODE_TYPE
					String key = propertykeys[2];
					Long value = oldParentNodeType.get(key);
					oldParentNodeType.put(key, value == null ? 1 : value + 1);
					
					// PROPERTY_NAME
					key = propertykeys[6];
					value = propertyName.get(key);
					propertyName.put(key, value == null ? 1 : value + 1);
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
		
		StringBuilder stats = new StringBuilder();
		stats.append("\t _______________________________________________________________________ \n"
				+ "\t| " + String.format("%-70s", "UPDATE PROPERTY ...") + " |\n");
		
		// OLD_PARENT_NODE_TYPE
		stats.append("\t| " + String.format("%-70s", "") + " |\n");
		stats.append("\t| " + String.format("%-70s", "OLD_PARENT_NODE_TYPE") + " |\n");
		Iterator it = MapUtil.sortByValue(oldParentNodeType).entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        stats.append("\t| " + String.format("%-70s", "") + " |\n"
					+ "\t| > " + String.format("%-53s", pair.getKey()) + String.format("%15s", pair.getValue()) + " |\n");
	    }
	    
	    // PROPERTY_NAME
	    stats.append("\t| " + String.format("%-70s", "") + " |\n");
 		stats.append("\t| " + String.format("%-70s", "PROPERTY_NAME") + " |\n");
 		it = MapUtil.sortByValue(propertyName).entrySet().iterator();
 	    while (it.hasNext()) {
 	        Map.Entry pair = (Map.Entry)it.next();
 	        stats.append("\t| " + String.format("%-70s", "") + " |\n"
 					+ "\t| > " + String.format("%-53s", pair.getKey()) + String.format("%15s", pair.getValue()) + " |\n");
 	    }
	 	    
		stats.append("\t ________________________________________________________________________/\n\n");
		
		System.out.println(stats);
	}

	public static void main(String[] args) {
		StatBot statBot = new StatBot("logs_tiles", "export_tiles");
		statBot.printLogStats();
		statBot.printDeletePropertyStats();
		statBot.printDeleteNodeStats();
		statBot.printInsertNodeStats();
		statBot.printInsertPropertyStats();
		statBot.printUpdatePropertyStats();
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
