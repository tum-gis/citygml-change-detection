@echo off
set PATH=C:\Program Files\PuTTY
set myvar=%cd%
echo Please make sure the folder CityGMLChangeDetection already exists in the server.
pscp -r %myvar% nguyen@ServerIP:/home/nguyen/RAMDISK/CityGMLChangeDetection
echo Data transfer to server finished.
pause