#!/bin/bash

move_dir() {
    local source="$1"
    local target="$2"
    local retries=20
    while [ $retries -gt 0 ]; do
        mv "$source" "$target" >/dev/null 2>&1
        if [ $? -eq 0 ]; then
            return 0
        fi
        ((retries--))
        echo "Access denied on move ($source -> $target); retrying in 1 second... ($retries retries left)"
        sleep 1
    done
    echo "Failed to move $source to $target after 20 retries."
    exit 1
}

remove_dir() {
    local dir="$1"
    local retries=20
    while [ $retries -gt 0 ]; do
        rm -rf "$dir" >/dev/null 2>&1
        if [ ! -d "$dir" ]; then
            return 0
        fi
        ((retries--))
        echo "Access denied on remove $dir; retrying in 1 second... ($retries retries left)"
        sleep 1
    done
    echo "Failed to remove $dir after 20 retries."
    exit 1
}

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
move_dir "$ROOT/com" "$ROOT/com.bak"

BACKED_UP_LIB=0
if [ -d "$ROOT/lib" ]; then
    if [ -d "$ROOT/lib.bak" ]; then
        rm -rf "$ROOT/lib.bak" >/dev/null 2>&1
    fi
    move_dir "$ROOT/lib" "$ROOT/lib.bak"
    BACKED_UP_LIB=1
fi

echo "Running UpgradeTool from temporary directory..."
java -cp "$CP" com.planet_ink.coffee_mud.application.UpgradeTool "$@"
TOOL_ERROR=$?

# Cleanup temp directory
rm -rf "$TEMP_DIR" >/dev/null 2>&1
echo "Temporary directory cleaned up."

if [ $TOOL_ERROR -eq 0 ]; then
    echo "Upgrade successful. Deleting backups..."
    remove_dir "$ROOT/com.bak"
    if [ -d "$ROOT/lib.bak" ]; then
        remove_dir "$ROOT/lib.bak"
    fi
else
    echo "Upgrade failed. Restoring backups..."
    remove_dir "$ROOT/com"
    move_dir "$ROOT/com.bak" "$ROOT/com"
    if [ $BACKED_UP_LIB -eq 1 ]; then
        remove_dir "$ROOT/lib"
        move_dir "$ROOT/lib.bak" "$ROOT/lib"
    else
        if [ -d "$ROOT/lib" ]; then
            remove_dir "$ROOT/lib"
        fi
    fi
fi

exit 0