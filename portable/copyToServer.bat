@echo off
set PATH=C:\Program Files\PuTTY
set myvar=%cd%
echo The folder CityGMLChangeDetection must exist in server...
pscp -r %myvar% nguyen@10.162.246.20:/home/nguyen/RAMDISK/CityGMLChangeDetection
echo Data transfer to server finished.
pause