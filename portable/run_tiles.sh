#!/bin/bash


# test_data/2016/LOD1-Nordrhein-Westfalen/LoD1_280_5657_1.gml
# test_data/2018/LOD1-Nordrhein-Westfalen/LoD1_280_5657_1_NW.gml

A_test_data_location="test_data/2016/LOD1-Nordrhein-Westfalen/"
B_test_data_location="test_data/2018/LOD1-Nordrhein-Westfalen/"

# Count files
AB_total_count=0
for A in $A_test_data_location*.gml; do
	AB_total_count=$(($AB_total_count + 1))
done
for B in $B_test_data_location*.gml; do
	AB_total_count=$(($AB_total_count + 1))
done

# Machted tiles
AB_current_count=0
echo       MATCHING TILES IN BOTH OLD AND NEW DATASETS
tile_suffix="_NW"
for A in $A_test_data_location*.gml; do
	A_without_path="${A##*/}"
	A_without_ext="${A_without_path%.*}"
	for B in $B_test_data_location*.gml; do
		B_without_path="${B##*/}"
		B_without_ext="${B_without_path%.*}"
		if [ ${B_without_ext/_NW/""} == $A_without_ext ]; then
			# Both files are matched
			mkdir "export_tiles/$A_without_ext"
			
			# Adjust the config file for each matched tile pair
			settings_tiles_location="settings_tiles/$A_without_ext.txt"
			cp "settings/Config_Tiles.txt" "$settings_tiles_location"
			
			sed -i "s/OLD_CITY_MODEL_LOCATION=/OLD_CITY_MODEL_LOCATION=2016\/LOD1-Nordrhein-Westfalen\/$A_without_path/g" "$settings_tiles_location"
			sed -i "s/NEW_CITY_MODEL_LOCATION=/NEW_CITY_MODEL_LOCATION=2018\/LOD1-Nordrhein-Westfalen\/$B_without_path/g" "$settings_tiles_location"
			sed -i "s/DB_LOCATION=neo4jDB_tiles\//DB_LOCATION=neo4jDB_tiles\/$A_without_ext\//g" "$settings_tiles_location"
			sed -i "s/LOG_LOCATION=logs_tiles\/Default.log/LOG_LOCATION=logs_tiles\/$A_without_ext.log/g" "$settings_tiles_location"
			sed -i "s/EXPORT_LOCATION=export_tiles\//EXPORT_LOCATION=export_tiles\/$A_without_ext\//g" "$settings_tiles_location"
			
			# Execute change detection for this tile pair
			AB_current_count=$(($AB_current_count + 2))
			echo [$(($AB_current_count*100/$AB_total_count))%] --- matching tiles: [$A_without_ext] -- [$B_without_ext]
			java -Xms4g -Xmx4g -XX:+UseG1GC -jar citygml-change-detection-transactions.jar -SETTINGS="$settings_tiles_location" >console_backup/logs_tiles/$A_without_ext.txt 2>&1
			# java -Xms4g -Xmx4g -XX:+UseG1GC -jar citygml-change-detection-transactions.jar -SETTINGS="$settings_tiles_location"
		fi
	done
done

# Tiles only in older datasets
echo       TILES ONLY IN OLD DATASETS
for A in $A_test_data_location*.gml; do
	A_found="false"
	A_without_path="${A##*/}"
	A_without_ext="${A_without_path%.*}"
	for B in $B_test_data_location*.gml; do
		B_without_path="${B##*/}"
		B_without_ext="${B_without_path%.*}"
		if [ ${B_without_ext/_NW/""} == $A_without_ext ]; then
			A_found="true"
		fi
	done

	if [ $A_found == "false" ]; then
		mkdir "export_tiles_only_in_old/$A_without_ext"
			
		# Adjust the config file for each tile
		settings_tiles_location="settings_tiles_only_in_old/$A_without_ext.txt"
		cp "settings/Config_Tiles.txt" "$settings_tiles_location"
			
		sed -i "s/OLD_CITY_MODEL_LOCATION=/OLD_CITY_MODEL_LOCATION=2016\/LOD1-Nordrhein-Westfalen\/$A_without_path/g" "$settings_tiles_location"
		sed -i "s/DB_LOCATION=neo4jDB_tiles\//DB_LOCATION=neo4jDB_tiles_only_in_old\/$A_without_ext\//g" "$settings_tiles_location"
		sed -i "s/LOG_LOCATION=logs_tiles\/Default.log/LOG_LOCATION=logs_tiles_only_in_old\/$A_without_ext.log/g" "$settings_tiles_location"
		sed -i "s/EXPORT_LOCATION=export_tiles\//EXPORT_LOCATION=export_tiles_only_in_old\/$A_without_ext\//g" "$settings_tiles_location"
		
		# Execute change detection for this tile
		AB_current_count=$(($AB_current_count + 1))
		echo [$(($AB_current_count*100/$AB_total_count))%] --- mapping tile: [$A_without_ext]
		java -Xms4g -Xmx4g -XX:+UseG1GC -jar citygml-change-detection-transactions.jar -SETTINGS="$settings_tiles_location" >console_backup/logs_tiles_only_in_old/$A_without_ext.txt 2>&1
		# java -Xms4g -Xmx4g -XX:+UseG1GC -jar citygml-change-detection-transactions.jar -SETTINGS="$settings_tiles_location"
	fi
done

# Tiles only in newer datasets
echo       TILES ONLY IN NEW DATASETS
for B in $B_test_data_location*.gml; do
	B_found="false"
	B_without_path="${B##*/}"
	B_without_ext="${B_without_path%.*}"
	for A in $A_test_data_location*.gml; do
		A_without_path="${A##*/}"
		A_without_ext="${A_without_path%.*}"
		if [ ${B_without_ext/_NW/""} == $A_without_ext ]; then
			B_found="true"
		fi
	done

	if [ $B_found == "false" ]; then
		mkdir "export_tiles_only_in_new/$B_without_ext"
			
		# Adjust the config file for each tile
		settings_tiles_location="settings_tiles_only_in_new/$B_without_ext.txt"
		cp "settings/Config_Tiles.txt" "$settings_tiles_location"
			
		sed -i "s/OLD_CITY_MODEL_LOCATION=/OLD_CITY_MODEL_LOCATION=2018\/LOD1-Nordrhein-Westfalen\/$B_without_path/g" "$settings_tiles_location"
		sed -i "s/DB_LOCATION=neo4jDB_tiles\//DB_LOCATION=neo4jDB_tiles_only_in_new\/$B_without_ext\//g" "$settings_tiles_location"
		sed -i "s/LOG_LOCATION=logs_tiles\/Default.log/LOG_LOCATION=logs_tiles_only_in_new\/$B_without_ext.log/g" "$settings_tiles_location"
		sed -i "s/EXPORT_LOCATION=export_tiles\//EXPORT_LOCATION=export_tiles_only_in_new\/$B_without_ext\//g" "$settings_tiles_location"
		
		# Execute change detection for this tile
		AB_current_count=$(($AB_current_count + 1))
		echo [$(($AB_current_count*100/$AB_total_count))%] --- mapping tile: [$B_without_ext]
		java -Xms4g -Xmx4g -XX:+UseG1GC -jar citygml-change-detection-transactions.jar -SETTINGS="$settings_tiles_location" >console_backup/logs_tiles_only_in_new/$B_without_ext.txt 2>&1
		# java -Xms4g -Xmx4g -XX:+UseG1GC -jar citygml-change-detection-transactions.jar -SETTINGS="$settings_tiles_location"
	fi
done

read -p "DONE. Press enter to continue"

