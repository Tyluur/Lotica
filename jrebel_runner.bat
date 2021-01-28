@echo off
@title Lotica
:run
java -javaagent:C:\Users\Administrator\Dropbox\jrebel.jar -Xmx6G -Xms2048m -server -XX:+UseG1GC -cp bin;data/essentials/libs/* com.runescape.Main