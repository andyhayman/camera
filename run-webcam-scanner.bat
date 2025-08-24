@echo off
echo ==========================================
echo    WEBCAM BARCODE SCANNER - LIVE CAMERA
echo ==========================================
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
    echo Compiling webcam application...
    javac -cp "lib/*" -d classes src/main/java/com/example/barcodescanner/WebcamBarcodeScanner.java
    if %errorlevel% neq 0 (
        echo ERROR: Compilation failed
        pause
        exit /b 1
    )
    echo Compilation successful!
)

echo Starting Webcam Barcode Scanner...
echo.
echo LIVE CAMERA INSTRUCTIONS:
echo 1. Click "Start Camera" to activate your webcam
echo 2. Point camera at any barcode or QR code
echo 3. Detection happens automatically - no clicking needed!
echo 4. Results appear instantly with sound notification
echo 5. Click "Stop Camera" when finished
echo.
echo SUPPORTED FORMATS:
echo - QR Codes, Code 128, EAN-13, UPC-A, and 15+ more!
echo - Works with product barcodes, QR codes, etc.
echo.
echo TIPS:
echo - Ensure good lighting
echo - Hold barcode 6-12 inches from camera
echo - Keep barcode flat and steady
echo.
echo NOTE: If camera doesn't work, check:
echo - Camera permissions in Windows settings
echo - Close other apps using the camera
echo - Try the image-file version: run-working-gui.bat
echo.

java -cp "classes;lib/*" com.example.barcodescanner.WebcamBarcodeScanner

echo.
echo Webcam scanner closed.
pause
