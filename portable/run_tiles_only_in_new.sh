#!/bin/bash

# First argument: one input array of tile files (names without path, only extension) only in new datasets

A_array=()
IFS=',' read -r -a A_array <<< "$1"

for A_without_path in "${A_array[@]}"; do
	A_without_ext="${A_without_path%.*}"
	mkdir "export_tiles_only_in_new/$A_without_ext"
		
	# Adjust the config file for each tile
	settings_tiles_location="settings_tiles_only_in_new/$A_without_ext.txt"
	cp "settings/Config_Tiles.txt" "$settings_tiles_location"
		
	sed -i "s/OLD_CITY_MODEL_LOCATION=/OLD_CITY_MODEL_LOCATION=2018\/LOD1-Nordrhein-Westfalen\/$A_without_path/g" "$settings_tiles_location"
	sed -i "s/DB_LOCATION=neo4jDB_tiles\//DB_LOCATION=neo4jDB_tiles_only_in_new\/$A_without_ext\//g" "$settings_tiles_location"
	sed -i "s/LOG_LOCATION=logs_tiles\/Default.log/LOG_LOCATION=logs_tiles_only_in_new\/$A_without_ext.log/g" "$settings_tiles_location"
	sed -i "s/EXPORT_LOCATION=export_tiles\//EXPORT_LOCATION=export_tiles_only_in_new\/$A_without_ext\//g" "$settings_tiles_location"
	
	# Execute change detection for this tile
	echo -------- mapping tile: [$A_without_ext]
	java -Xms4g -Xmx4g -XX:+UseG1GC -jar citygml-change-detection-transactions.jar -SETTINGS="$settings_tiles_location" >console_backup/logs_tiles_only_in_new/$A_without_ext.txt 2>&1
	# java -Xms4g -Xmx4g -XX:+UseG1GC -jar citygml-change-detection-transactions.jar -SETTINGS="$settings_tiles_location"
done

