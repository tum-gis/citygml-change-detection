@echo off
echo Deleting existing Neo4j Database...
@echo off
rmdir /Q /S neo4jDB
@echo off
java -Xms1000m -Xmx1000m -XX:+UseG1GC -jar CityGMLChangeDetection.jar -SETTINGS="Settings.txt"
@echo off
rmdir /S neo4jDB