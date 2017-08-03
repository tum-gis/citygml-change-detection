#!/bin/bash
echo "Deleting old Neo4J database..."
rm -rf neo4jDB
if [ -f Settings.txt ]; then
	java -Xms30000m -Xmx30000m -XX:+UseG1GC -jar CityGMLChangeDetection.jar -SETTINGS="Settings.txt"
	rm -i -r neo4jDB
else
	echo "Please place Settings.txt in the same folder as run.sh file and run it again."
fi