#!/bin/bash
printf "Deleting existing Neo4j database...\n\n"
rm -rf neo4jDB
if [ -f Settings.txt ]; then
	java -Xms30000m -Xmx30000m -XX:+UseG1GC -jar CityGMLChangeDetection.jar -SETTINGS="settings/Default.txt"
	printf "\nDelete Neo4j database?\n"
	select yn in "Yes" "No"; do
		case $yn in
			Yes ) printf "Deleting Neo4j database...\n"; rm -rf neo4jDB; break;;
			No ) break;;
		esac
	done
	printf "\nExecution done.\n\n"
else
	printf "Please place Settings.txt in the same folder as run.sh file and run it again.\n\n"
fi