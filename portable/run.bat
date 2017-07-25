echo Deleting existing Neo4j Database...
rmdir /Q /S neo4jDB
java -Xms1000m -Xmx1000m -XX:+UseG1GC -jar CityGMLChangeDetection_Portable.jar -SETTINGS="Settings.txt"
rmdir /S neo4jDB