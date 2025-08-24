@echo off
echo Compiling Barcode Scanner Application...
echo.

REM Create lib directory for dependencies
if not exist "lib" mkdir lib

REM Create classes directory
if not exist "classes" mkdir classes

echo Downloading dependencies...
echo Note: This requires Maven to download dependencies automatically.
echo If Maven is not available, please download the following JAR files manually:
echo.
echo 1. JavaFX Controls and FXML (from https://openjfx.io/)
echo 2. ZXing Core and JavaSE (from https://github.com/zxing/zxing)
echo 3. Webcam Capture (from https://github.com/sarxos/webcam-capture)
echo.
echo Place all JAR files in the 'lib' directory and run this script again.
echo.

REM Check if lib directory has JAR files
set JAR_COUNT=0
for %%f in (lib\*.jar) do set /a JAR_COUNT+=1

if %JAR_COUNT% equ 0 (
    echo No JAR files found in lib directory.
    echo Please download the required dependencies as mentioned above.
    echo.
    echo Alternatively, install Maven and run: mvn clean compile
    pause
    exit /b 1
)

echo Found %JAR_COUNT% JAR files in lib directory.
echo Compiling Java source files...

REM Build classpath
set CLASSPATH=classes
for %%f in (lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%f

REM Compile Java files
javac -cp "%CLASSPATH%" -d classes src\main\java\com\example\barcodescanner\*.java

if %errorlevel% neq 0 (
    echo ERROR: Compilation failed
    pause
    exit /b 1
)

echo Compilation successful!
echo.
echo To run the application:
echo java -cp "classes;lib\*" com.example.barcodescanner.BarcodeScanner
echo.
pause
