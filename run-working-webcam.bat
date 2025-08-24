@echo off
echo ==========================================
echo    WORKING WEBCAM BARCODE SCANNER
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
    echo Compiling working webcam application...
    javac -cp "lib/*" -d classes src/main/java/com/example/barcodescanner/WorkingWebcamScanner.java
    if %errorlevel% neq 0 (
        echo ERROR: Compilation failed
        pause
        exit /b 1
    )
    echo Compilation successful!
)

echo Starting Working Webcam Barcode Scanner...
echo.
echo ✅ WEBCAM FIXED - READY FOR LIVE SCANNING!
echo.
echo FEATURES:
echo 🎯 Live camera barcode scanning
echo 🔊 Sound notifications on detection
echo 📱 Popup alerts with barcode content
echo 🎨 Visual feedback (green border flash)
echo 📊 17+ barcode formats supported
echo ⚡ Real-time detection
echo.
echo INSTRUCTIONS:
echo 1. Click "🚀 Start Live Scanning" in the application
echo 2. Point your camera at any barcode or QR code
echo 3. Listen for beep sound when barcode is detected
echo 4. See popup notification with barcode content
echo 5. Results appear in the bottom panel
echo 6. Click "⏹ Stop Scanning" when finished
echo.
echo TIPS:
echo • Ensure good lighting
echo • Hold barcode 6-12 inches from camera
echo • Keep barcode flat and steady
echo • Works with QR codes, product barcodes, etc.
echo.

java -cp "classes;lib/*" com.example.barcodescanner.WorkingWebcamScanner

echo.
echo Working webcam scanner closed.
pause
