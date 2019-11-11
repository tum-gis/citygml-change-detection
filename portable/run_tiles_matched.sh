#!/bin/bash

# Argument: one input array of matched tile files (names with path and extension)

AB_array=()
IFS=',' read -r -a AB_array <<< "$1"
AB_size=${#AB_array[@]}

for ((AB_i=0; AB_i < AB_size; AB_i++)); do
	AB_without_path="${AB_array[AB_i]##*/}"
	AB_without_ext="${AB_without_path%.*}"
	mkdir "export_tiles/$AB_without_ext"
	# Adjust the config file for each matched tile pair
	settings_tiles_location="settings_tiles/$AB_without_ext.txt"
	cp "settings/Config_Tiles.txt" "$settings_tiles_location"
	
	sed -i "s/OLD_CITY_MODEL_LOCATION=/OLD_CITY_MODEL_LOCATION=2016\/LOD1-Nordrhein-Westfalen\/$AB_without_path/g" "$settings_tiles_location"
	sed -i "s/NEW_CITY_MODEL_LOCATION=/NEW_CITY_MODEL_LOCATION=2018\/LOD1-Nordrhein-Westfalen\/$AB_without_path/g" "$settings_tiles_location"
	sed -i "s/DB_LOCATION=neo4jDB_tiles\//DB_LOCATION=neo4jDB_tiles\/$AB_without_ext\//g" "$settings_tiles_location"
	sed -i "s/LOG_LOCATION=logs_tiles\/Default.log/LOG_LOCATION=logs_tiles\/$AB_without_ext.log/g" "$settings_tiles_location"
	sed -i "s/EXPORT_LOCATION=export_tiles\//EXPORT_LOCATION=export_tiles\/$AB_without_ext\//g" "$settings_tiles_location"
	
	# Execute change detection for this tile pair
	echo -------- matching tiles: [$AB_without_ext]
	java -Xms4g -Xmx4g -XX:+UseG1GC -jar citygml-change-detection-transactions.jar -SETTINGS="$settings_tiles_location" >console_backup/logs_tiles/$AB_without_ext.txt 2>&1
	# java -Xms4g -Xmx4g -XX:+UseG1GC -jar citygml-change-detection-transactions.jar -SETTINGS="$settings_tiles_location"
done

