REM ** Make sure you edit the name of your mud at the end of this batch file first!
REM ** You may need to modify the word java here to point to your java.exe with a proper
REM **  path.  E.G. c:\j2sdk1.4.2_16\bin\java
java -classpath ".;.\lib\js.jar;.\lib\jzlib.jar" -Xms64m -Xmx128m com.planet_ink.coffee_mud.application.MUD "Your Muds Name"
