rm -rf neo4jDB
java -Xms30000m -Xmx30000m -XX:+UseG1GC -jar CityGMLChangeDetection.jar -SETTINGS="Settings.txt"
rm -i -r neo4jDB