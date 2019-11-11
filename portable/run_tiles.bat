@echo off
:: test_data/2016/LOD1-Nordrhein-Westfalen/LoD1_280_5657_1.gml
:: test_data/2018/LOD1-Nordrhein-Westfalen/LoD1_280_5657_1_NW.gml

setlocal enableextensions disabledelayedexpansion

:: Tiles only in older dataset
echo MATCHING TILES ONLY IN OLD DATASETS
for %%A in (test_data/2016/LOD1-Nordrhein-Westfalen/*.gml) do (
	set "found=false"
	for %%B in (test_data/2018/LOD1-Nordrhein-Westfalen/*.gml) do (
		if %%~nB==%%~nA_NW set "!found!=true"
	)
	
	if "!found!" == "false" (
		echo %%A
		md "export_tiles_only_in_old/%%~nA"
		
		:: Adjust the config file for this tile
		>settings_tiles_only_in_old/%%~nA.txt (
			for /f "usebackq delims=" %%C in (settings/Config_Tiles.txt) do (
				if "%%C" equ "OLD_CITY_MODEL_LOCATION=" (echo OLD_CITY_MODEL_LOCATION=2016/LOD1-Nordrhein-Westfalen/%%A) else (
			    	if "%%C" equ "NEW_CITY_MODEL_LOCATION=" (echo NEW_CITY_MODEL_LOCATION=) else (
						if "%%C" equ "DB_LOCATION=neo4jDB_tiles/" (echo DB_LOCATION=neo4jDB_tiles_only_in_old/%%~nA/) else (
							if "%%C" equ "LOG_LOCATION=logs_tiles/Default.log" (echo LOG_LOCATION=logs_tiles_only_in_old/%%~nA.log) else (
								if "%%C" equ "EXPORT_LOCATION=export_tiles/" (echo EXPORT_LOCATION=export_tiles_only_in_old/%%~nA/) else (
									if "%%C" equ "RTREE_IMAGE_LOCATION=saved_pictures_tiles/" (echo RTREE_IMAGE_LOCATION=saved_pictures_tiles_only_in_old/%%~nA/) else (echo %%C)
								)
							)
						)
					)
			    )
			)
		)
		
		:: Execute mapping for this tile
		::java -Xms4g -Xmx4g -XX:+UseG1GC -jar citygml-change-detection-transactions.jar -SETTINGS="settings_tiles_only_in_old/%%~nA.txt"
	)
)


:: Tiles only in newer dataset
echo MATCHING TILES ONLY IN NEW DATASETS
for %%B in (test_data/2018/LOD1-Nordrhein-Westfalen/*.gml) do (
	set "found=false"
	for %%A in (test_data/2016/LOD1-Nordrhein-Westfalen/*.gml) do (
		if %%~nB==%%~nA_NW set "found=true"
	)
	
	if "%found%" == "false" (
		echo %%B
		md "export_tiles_only_in_new/%%~nB"
		
		:: Adjust the config file for this tile
		>settings_tiles_only_in_new/%%~nB.txt (
			for /f "usebackq delims=" %%C in (settings/Config_Tiles.txt) do (
			    if "%%C" equ "OLD_CITY_MODEL_LOCATION=" (echo OLD_CITY_MODEL_LOCATION=2018/LOD1-Nordrhein-Westfalen/%%B) else (
			    	if "%%C" equ "NEW_CITY_MODEL_LOCATION=" (echo NEW_CITY_MODEL_LOCATION=) else (
						if "%%C" equ "DB_LOCATION=neo4jDB_tiles/" (echo DB_LOCATION=neo4jDB_tiles_only_in_new/%%~nB/) else (
							if "%%C" equ "LOG_LOCATION=logs_tiles/Default.log" (echo LOG_LOCATION=logs_tiles_only_in_new/%%~nB.log) else (
								if "%%C" equ "EXPORT_LOCATION=export_tiles/" (echo EXPORT_LOCATION=export_tiles_only_in_new/%%~nB/) else (
									if "%%C" equ "RTREE_IMAGE_LOCATION=saved_pictures_tiles/" (echo RTREE_IMAGE_LOCATION=saved_pictures_tiles_only_in_new/%%~nB/) else (echo %%C)
								)
							)
						)
					)
				)
			)
		)
		
		:: Execute mapping for this tile
		::java -Xms4g -Xmx4g -XX:+UseG1GC -jar citygml-change-detection-transactions.jar -SETTINGS="settings_tiles_only_in_new/%%~nB.txt"
	)
)

:: Machted tiles
echo MATCHING TILES IN BOTH OLD AND NEW DATASETS
for %%A in (test_data/2016/LOD1-Nordrhein-Westfalen/*.gml) do (
	for %%B in (test_data/2018/LOD1-Nordrhein-Westfalen/*.gml) do (
		echo %%A %%B
		
		if %%~nB==%%~nA_NW (
			:: Both files are matched
			echo CHECK %%A %%B
			
			md "export_tiles/%%~nA"
		
			:: Adjust the config file for each matched tile pair
			>settings_tiles/%%~nA.txt (
				for /f "usebackq delims=" %%C in (settings/Config_Tiles.txt) do (
				    if "%%C" equ "OLD_CITY_MODEL_LOCATION=" (echo OLD_CITY_MODEL_LOCATION=2016/LOD1-Nordrhein-Westfalen/%%A) else (
				    	if "%%C" equ "NEW_CITY_MODEL_LOCATION=" (echo NEW_CITY_MODEL_LOCATION=2018/LOD1-Nordrhein-Westfalen/%%B) else (
							if "%%C" equ "DB_LOCATION=neo4jDB_tiles/" (echo DB_LOCATION=neo4jDB_tiles/%%~nA/) else (
								if "%%C" equ "LOG_LOCATION=logs_tiles/Default.log" (echo LOG_LOCATION=logs_tiles/%%~nA.log) else (
									if "%%C" equ "EXPORT_LOCATION=export_tiles/" (echo EXPORT_LOCATION=export_tiles/%%~nA/) else (
										if "%%C" equ "RTREE_IMAGE_LOCATION=saved_pictures_tiles/" (echo RTREE_IMAGE_LOCATION=saved_pictures_tiles/%%~nA/) else (echo %%C)
									)
								)
				    		)
				  		)
					)
				)
			)
			
			:: Execute change detection for this tile pair
					java -Xms4g -Xmx4g -XX:+UseG1GC -jar citygml-change-detection-transactions.jar -SETTINGS="settings_tiles/%%~nA.txt"
		)
	)
)

pause