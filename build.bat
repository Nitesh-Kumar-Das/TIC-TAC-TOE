@echo off
:: Build script for Windows
:: Requires JDK 17 or higher with jlink and jpackage in the PATH
:: Requires WiX Toolset installed and in the PATH to generate EXE/MSI installers

echo === Starting Build Process ===

:: 1. Clean previous build artifacts
echo Cleaning old build directories...
if exist out rmdir /s /q out
if exist dist rmdir /s /q dist

:: 2. Compile source files
echo Compiling Java source files...
mkdir out\classes
javac -d out\classes AIBot.java GameLogic.java TicTacToeUI.java
if %errorlevel% neq 0 (
    echo ERROR: Compilation failed.
    exit /b %errorlevel%
)

:: 3. Create a runnable JAR
echo Creating runnable JAR...
jar --create --file out\game.jar --main-class=TicTacToeUI -C out\classes .
if %errorlevel% neq 0 (
    echo ERROR: JAR creation failed.
    exit /b %errorlevel%
)

:: 4. Create trimmed JRE runtime with jlink
echo Creating custom JRE using jlink...
jlink --add-modules java.desktop ^
      --strip-debug ^
      --no-header-files ^
      --no-man-pages ^
      --output out\custom-jre
if %errorlevel% neq 0 (
    echo ERROR: jlink failed.
    exit /b %errorlevel%
)

:: 5. Package with jpackage
echo Packaging for Windows (EXE)...
mkdir dist

:: Verify candle.exe/light.exe are available from WiX Toolset
where candle.exe >nul 2>nul
if %errorlevel% neq 0 (
    echo WARNING: WiX Toolset (candle.exe) was not found in your PATH.
    echo jpackage will fail to generate EXE/MSI without it.
    echo Please install WiX Toolset 3.x and add it to your PATH.
)

jpackage --name "TicTacToeUltimate" ^
         --input out ^
         --main-jar game.jar ^
         --main-class TicTacToeUI ^
         --runtime-image out\custom-jre ^
         --dest dist ^
         --icon icons\icon.ico ^
         --type exe ^
         --win-shortcut ^
         --win-menu ^
         --win-menu-group "Games"
         
if %errorlevel% neq 0 (
    echo ERROR: jpackage failed.
    exit /b %errorlevel%
)

echo Build complete! EXE package created in dist\
pause
