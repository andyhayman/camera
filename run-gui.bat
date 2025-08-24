@echo off
echo Starting Barcode Scanner GUI Application...
echo.

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 11 or higher and try again
    pause
    exit /b 1
)

REM Check if classes directory exists
if not exist "classes" (
    echo Classes directory not found. Compiling application...
    javac -cp "lib/*" -d classes src/main/java/com/example/barcodescanner/SwingBarcodeScanner.java
    if %errorlevel% neq 0 (
        echo ERROR: Compilation failed
        pause
        exit /b 1
    )
    echo Compilation successful!
)

REM Check if lib directory has JAR files
set JAR_COUNT=0
for %%f in (lib\*.jar) do set /a JAR_COUNT+=1

if %JAR_COUNT% equ 0 (
    echo No JAR files found in lib directory.
    echo Please ensure dependencies are downloaded.
    pause
    exit /b 1
)

echo Starting GUI application...
echo.
echo Instructions:
echo 1. Click "Start Scanning" to activate your camera
echo 2. Point camera at a barcode or QR code
echo 3. Results will appear in the bottom panel
echo 4. Click "Stop Scanning" when finished
echo.

java -cp "classes;lib/*" com.example.barcodescanner.SwingBarcodeScanner

echo.
echo Application closed.
pause
