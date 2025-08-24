@echo off
echo ========================================
echo    BARCODE SCANNER - WORKING VERSION
echo ========================================
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
    echo Compiling application...
    javac -cp "lib/*" -d classes src/main/java/com/example/barcodescanner/NoWebcamBarcodeScanner.java
    if %errorlevel% neq 0 (
        echo ERROR: Compilation failed
        pause
        exit /b 1
    )
    echo Compilation successful!
)

echo Starting Barcode Scanner GUI...
echo.
echo INSTRUCTIONS:
echo 1. Click "Load Image" to select a barcode image file
echo 2. Click "Scan Barcode" to detect barcodes
echo 3. Results will appear in the bottom panel
echo.
echo SUPPORTED FORMATS:
echo - QR Codes, Code 128, EAN-13, UPC-A, and 13+ more!
echo - Image files: JPG, PNG, GIF, BMP
echo.
echo TIP: Use your phone to display a QR code, take a screenshot,
echo      and load that image file to test the scanner!
echo.

java -cp "classes;lib/*" com.example.barcodescanner.NoWebcamBarcodeScanner

echo.
echo Application closed.
pause
