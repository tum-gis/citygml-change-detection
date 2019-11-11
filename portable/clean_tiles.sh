#!/bin/bash
read -p "Delete exports, logs and neo4jDBs? " yn
case $yn in
	[Yy]* ) 
		rm -r export_tiles
		mkdir export_tiles
		rm -r export_tiles_only_in_old
		mkdir export_tiles_only_in_old
		rm -r export_tiles_only_in_new
		mkdir export_tiles_only_in_new

		rm -r logs_tiles
		mkdir logs_tiles
		rm -r logs_tiles_only_in_old
		mkdir logs_tiles_only_in_old
		rm -r logs_tiles_only_in_new
		mkdir logs_tiles_only_in_new

		rm -r neo4jDB_tiles
		mkdir neo4jDB_tiles
		rm -r neo4jDB_tiles_only_in_old
		mkdir neo4jDB_tiles_only_in_old
		rm -r neo4jDB_tiles_only_in_new
		mkdir neo4jDB_tiles_only_in_new

		rm -r saved_pictures_tiles
		mkdir saved_pictures_tiles
		rm -r saved_pictures_tiles_only_in_old
		mkdir saved_pictures_tiles_only_in_old
		rm -r saved_pictures_tiles_only_in_new
		mkdir saved_pictures_tiles_only_in_new

		rm -r settings_tiles
		mkdir settings_tiles
		rm -r settings_tiles_only_in_old
		mkdir settings_tiles_only_in_old
		rm -r settings_tiles_only_in_new
		mkdir settings_tiles_only_in_new 
		
		echo Cleaned up ;;
	[Nn]* ) echo No cleanup ;;
	* ) echo "Please answer yes or no.";;
esac

