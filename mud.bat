REM ** Make sure you edit the name of your mud at the end of this batch file first!
REM ** You may need to modify the word java here to point to your java.exe with a proper
REM **  path.  E.G. c:\jdk1.6.0_01\bin\java -- make sure you are building with java v1.6 or higher!
java -classpath ".;.\lib\js.jar;.\lib\jzlib.jar;.\lib\mysql-connector-java-5.1.38-bin.jar" -Djava.awt.headless=true -Xms2048m -Xmx4096m com.planet_ink.coffee_mud.application.MUD "Axsonia"
