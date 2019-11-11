@echo off
echo Deleting existing Neo4j Database...
if exist neo4jDB rmdir /Q /S neo4jDB
java -Xms1000m -Xmx1000m -XX:+UseG1GC -jar citygml-change-detection.jar -SETTINGS="settings/Default.txt"
rmdir /S neo4jDB
echo Execution done.
pause