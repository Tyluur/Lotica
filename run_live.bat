@echo off
@title Lotica
:run
java -Xmx6G -Xms2048m -server -XX:+UseG1GC -cp bin;data/essentials/libs/* com.runescape.Main
goto run