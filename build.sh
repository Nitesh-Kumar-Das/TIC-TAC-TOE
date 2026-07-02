#!/bin/bash
set -e

# Build script for macOS/Linux
# Requires JDK 17 or higher with jlink and jpackage in the PATH

echo "=== Starting Build Process ==="

# 1. Clean previous build artifacts
echo "Cleaning old build directories..."
rm -rf out dist

# 2. Compile source files
echo "Compiling Java source files..."
mkdir -p out/classes
javac -encoding UTF-8 -d out/classes AIBot.java GameLogic.java TicTacToeUI.java

# 3. Create a runnable JAR
echo "Creating runnable JAR..."
jar --create --file out/game.jar --main-class=TicTacToeUI -C out/classes .

# 4. Create trimmed JRE runtime with jlink
echo "Creating custom JRE using jlink..."
jlink --add-modules java.desktop \
      --strip-debug \
      --no-header-files \
      --no-man-pages \
      --output out/custom-jre

# 5. Determine platform and package with jpackage
OS_TYPE="$(uname -s)"
echo "Detected OS: $OS_TYPE"

mkdir -p dist

if [ "$OS_TYPE" = "Darwin" ]; then
    echo "Packaging for macOS (DMG)..."
    # Ensure Xcode Command Line Tools are installed (required for macOS packaging)
    if ! xcode-select -p &>/dev/null; then
        echo "ERROR: Xcode Command Line Tools are not installed." >&2
        echo "Please run: xcode-select --install" >&2
        exit 1
    fi
    
    jpackage --name "TicTacToeUltimate" \
             --input out \
             --main-jar game.jar \
             --main-class TicTacToeUI \
             --runtime-image out/custom-jre \
             --dest dist \
             --icon icons/icon.icns \
             --type dmg
             
    echo "Build complete! DMG package created in dist/"
    
elif [ "$OS_TYPE" = "Linux" ]; then
    echo "Packaging for Linux (DEB)..."
    # Ensure dpkg-dev is installed (required for DEB packaging)
    if ! command -v dpkg &>/dev/null; then
        echo "ERROR: 'dpkg' tool is missing. Cannot package .deb." >&2
        echo "Please install dpkg-dev (e.g., sudo apt install dpkg-dev)" >&2
        exit 1
    fi

    jpackage --name "tictactoeultimate" \
             --input out \
             --main-jar game.jar \
             --main-class TicTacToeUI \
             --runtime-image out/custom-jre \
             --dest dist \
             --icon icons/icon.png \
             --type deb \
             --linux-shortcut \
             --linux-menu-group "Game"
             
    echo "Build complete! DEB package created in dist/"
else
    echo "ERROR: Unsupported operating system: $OS_TYPE" >&2
    exit 1
fi
