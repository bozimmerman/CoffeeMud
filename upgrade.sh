#!/bin/bash

# Set root directory (current dir)
ROOT="$PWD"

# Find JAVA_HOME if not set
if [ -z "$JAVA_HOME" ]; then
    # Try to find javac and derive JAVA_HOME
    JAVAC_PATH=$(command -v javac 2>/dev/null)
    if [ -n "$JAVAC_PATH" ]; then
        JAVAC_PATH=$(readlink -f "$JAVAC_PATH")
        JAVA_HOME=$(dirname "$(dirname "$JAVAC_PATH")")
        export JAVA_HOME
    fi
fi

TOOLS_JAR="$JAVA_HOME/lib/tools.jar"
# Check if tools.jar exists; if not, assume newer JDK and skip it
if [ -f "$TOOLS_JAR" ]; then
    CP="$TEMP_DIR:$TOOLS_JAR"
else
    CP="$TEMP_DIR"
    # Optional: Add a message for debugging
    echo "tools.jar not found; assuming JDK 9+ and proceeding without it."
fi

TEMP_DIR="/tmp/cmudupgradetool_$RANDOM"
mkdir -p "$TEMP_DIR/com/planet_ink/coffee_mud/application" >/dev/null 2>&1

# Copy UpgradeTool*.class files
cp "$ROOT/com/planet_ink/coffee_mud/application/UpgradeTool"*".class" "$TEMP_DIR/com/planet_ink/coffee_mud/application/" >/dev/null 2>&1

if [ ! -f "$TEMP_DIR/com/planet_ink/coffee_mud/application/UpgradeTool.class" ]; then
    echo "Failed to copy UpgradeTool class files."
    rm -rf "$TEMP_DIR" >/dev/null 2>&1
    exit 1
fi

echo "Backing up directories..."
if [ -d "$ROOT/com.bak" ]; then
    rm -rf "$ROOT/com.bak" >/dev/null 2>&1
fi

if [ -d "$ROOT/lib" ]; then
    if [ -d "$ROOT/lib.bak" ]; then
        rm -rf "$ROOT/lib.bak" >/dev/null 2>&1
    fi
fi

echo "Running UpgradeTool from temporary directory..."
java -cp "$CP" com.planet_ink.coffee_mud.application.UpgradeTool "$@"
TOOL_ERROR=$?

# Cleanup temp directory
rm -rf "$TEMP_DIR" >/dev/null 2>&1
echo "Temporary directory cleaned up."

if [ $TOOL_ERROR -eq 0 ]; then
    echo "Upgrade successful. Deleting backups..."
else
    echo "Upgrade failed. Restoring backups..."
    if [ -d "$ROOT/lib" ]; then
    fi
fi

exit 0