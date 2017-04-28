REM Make sure you are building with java v1.6 or higher
REM SET Java_Home="C:\Program Files\Java\jdk1.8.0_71\"

set CLASSPATH=.;C:\Program Files\Java\jdk1.8.0_71\lib\dt.jar;C:\Program Files\Java\jdk1.8.0_71\lib\tools.jar;.\lib\js.jar;.\lib\jzlib.jar
SET JAVACPATH="C:\Program Files\Java\jdk1.8.0_71\bin\javac" -g -nowarn -deprecation

IF "%1" == "docs" GOTO :DOCS

%JAVACPATH% com/planet_ink/coffee_mud/Libraries/*.java
%JAVACPATH% com/planet_ink/coffee_mud/Libraries/interfaces/*.java
%JAVACPATH% com/planet_ink/coffee_mud/Libraries/layouts/*.java
%JAVACPATH% com/planet_ink/coffee_mud/Libraries/mcppkgs/*.java

GOTO :FINISH

:DOCS

"%Java_Home%\bin\javadoc" -d .\docs -J-Xmx1024m -subpackages com.planet_ink.coffee_mud 

:FINISH
pause
