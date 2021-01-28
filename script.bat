@echo off
@title Script Runner
set /p cp=Enter Script Name:
java -Xmx6G -Xms2048m -server -XX:+UseG1GC -cp bin;data/essentials/libs/* com.runescape.utility.applications.console.script.%cp%
pause