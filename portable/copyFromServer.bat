@echo off
set PATH=C:\Program Files\PuTTY
echo Copying logs from server...
pscp -r nguyen@ServerIP:/home/nguyen/RAMDISK/CityGMLChangeDetection/portable/logs C:\GitWorkspace\citygml-change-detection\portable
echo Copying saved_pictures from server...
pscp -r nguyen@ServerIP:/home/nguyen/RAMDISK/CityGMLChangeDetection/portable/saved_pictures C:\GitWorkspace\citygml-change-detection\portable
echo Data transfer from server finished.
pause