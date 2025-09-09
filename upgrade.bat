@echo off
setlocal enabledelayedexpansion

rem Set root directory (current dir)
set ROOT=%CD%

rem Find JAVA_HOME if not set
if not defined JAVA_HOME (
    for /f "tokens=2*" %%a in ('reg query "HKLM\Software\JavaSoft\Java Development Kit" /v CurrentVersion 2^>nul') do set JDK_VER=%%b
    if defined JDK_VER (
        for /f "tokens=2*" %%a in ('reg query "HKLM\Software\JavaSoft\Java Development Kit\!JDK_VER!" /v JavaHome 2^>nul') do set JAVA_HOME=%%b
    )
)

set TOOLS_JAR=%JAVA_HOME%\lib\tools.jar
if not exist "%TOOLS_JAR%" (
    echo tools.jar not found at %TOOLS_JAR%. Ensure JDK is installed and JAVA_HOME is set correctly.
    exit /b 1
)

set TEMP_DIR=%TEMP%\cmudupgradetool_%RANDOM%
mkdir "%TEMP_DIR%\com\planet_ink\coffee_mud\application" >nul 2>&1

rem Copy UpgradeTool*.class files
copy "%ROOT%\com\planet_ink\coffee_mud\application\UpgradeTool*.class" "%TEMP_DIR%\com\planet_ink\coffee_mud\application" >nul 2>&1

if not exist "%TEMP_DIR%\com\planet_ink\coffee_mud\application\UpgradeTool.class" (
    echo Failed to copy UpgradeTool class files.
    rd /s /q "%TEMP_DIR%" >nul 2>&1
    exit /b 1
)

echo Backing up directories...
if exist "%ROOT%\com.bak" rd /s /q "%ROOT%\com.bak" >nul 2>&1
call :moveDir "%ROOT%\com" "%ROOT%\com.bak"

set BACKED_UP_LIB=0
if exist "%ROOT%\lib" (
    if exist "%ROOT%\lib.bak" rd /s /q "%ROOT%\lib.bak" >nul 2>&1
    call :moveDir "%ROOT%\lib" "%ROOT%\lib.bak"
    set BACKED_UP_LIB=1
)

echo Running UpgradeTool from temporary directory...
java -cp "%TEMP_DIR%;%TOOLS_JAR%" com.planet_ink.coffee_mud.application.UpgradeTool %*
set TOOL_ERROR=%ERRORLEVEL%

rem Cleanup temp directory
rd /s /q "%TEMP_DIR%" >nul 2>&1
echo Temporary directory cleaned up.

if %TOOL_ERROR% == 0 (
    echo Upgrade successful. Deleting backups...
    call :removeDir "%ROOT%\com.bak"
    if exist "%ROOT%\lib.bak" call :removeDir "%ROOT%\lib.bak"
) else (
    echo Upgrade failed. Restoring backups...
    call :removeDir "%ROOT%\com"
    call :moveDir "%ROOT%\com.bak" "%ROOT%\com"
    if %BACKED_UP_LIB%==1 (
        call :removeDir "%ROOT%\lib"
        call :moveDir "%ROOT%\lib.bak" "%ROOT%\lib"
    ) else (
        if exist "%ROOT%\lib" call :removeDir "%ROOT%\lib"
    )
)

endlocal
goto :eof

:moveDir
set "source=%~1"
set "target=%~2"
set retries=20
:retryMove
move "%source%" "%target%" >nul 2>&1
if not errorlevel 1 goto :eof
set /a retries-=1
if %retries%==0 (
    echo Failed to move %source% to %target% after 20 retries.
    exit /b 1
)
echo Access denied on move (%source% -> %target%); retrying in 1 second... (%retries% retries left)
timeout /t 1 /nobreak >nul
goto retryMove

:removeDir
set "dir=%~1"
set retries=20
:retryRemove
rd /s /q "%dir%" >nul 2>&1
if not exist "%dir%" goto :eof
set /a retries-=1
if %retries%==0 (
    echo Failed to remove %dir% after 20 retries.
    exit /b 1
)
echo Access denied on remove %dir%; retrying in 1 second... (%retries% retries left)
timeout /t 1 /nobreak >nul
goto retryRemove