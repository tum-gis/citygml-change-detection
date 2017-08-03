@echo off
set PATH=C:\Program Files\PuTTY

echo Copying logs from server...
pscp -r nguyen@ServerIP:/home/nguyen/RAMDISK/CityGMLChangeDetection/portable/logs C:\GitWorkspace\citygml-change-detection\portable

echo Copying exported CSV files from server...
pscp -r nguyen@ServerIP:/home/nguyen/RAMDISK/CityGMLChangeDetection/portable/export C:\GitWorkspace\citygml-change-detection\portable

set /P copyPics=Copy R-tree signature pictures? [y/n] 
if "%copyPics%"=="y" goto copy_pics
goto end_pics
:copy_pics
echo Copying saved_pictures from server...
pscp -r nguyen@ServerIP:/home/nguyen/RAMDISK/CityGMLChangeDetection/portable/saved_pictures C:\GitWorkspace\citygml-change-detection\portable
:end_pics

set /P copyDB=Copy Neo4j Database? [y/n] 
if "%copyDB%"=="y" goto copy_db
goto end_db
:copy_db
echo Copying Neo4j Database from server...
pscp -r nguyen@ServerIP:/home/nguyen/RAMDISK/CityGMLChangeDetection/portable/neo4jDB C:\GitWorkspace\citygml-change-detection\portable
:end_db

echo Data transfer from server finished.
pause