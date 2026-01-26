REM Make sure you are building with java v1.8 or higher
REM SET Java_Home="C:\Program Files\Java\jdk1.8.0_01"
set CLASSPATH=.;%Java_Home%\lib\dt.jar;%Java_Home%\lib\tools.jar;.\lib\js.jar;.\lib\jzlib.jar
SET JAVACPATH="%Java_Home%\bin\javac" -g -nowarn -deprecation

IF "%1" == "docs" GOTO :DOCS

@del /f sources.txt 2>nul
@del /s /f *.class 1>nul 2>nul
@setlocal EnableDelayedExpansion
@set "root=%CD%\"
@echo Gathering source files from %root%com...
@(for /r "com" %%a in (*.java) do @(
   set "file=%%a"
   echo !file:%root%=!
)) >> sources.txt
%JAVACPATH% @sources.txt
@del sources.txt


GOTO :FINISH

:DOCS

"%Java_Home%\bin\javadoc" -d .\docs -J-Xmx1024m -subpackages com.planet_ink.coffee_mud 

:FINISH
