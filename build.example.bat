:: Example build script
:: For usage, make a copy of it and customize the settings as build.bat
@echo off

set outputDirectory=C:\Users\USERNAME\Desktop\Server\plugins\

mvn clean package -Doutput.directory=%outputDirectory%