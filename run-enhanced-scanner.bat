@echo off
echo ==========================================
echo    ENHANCED WEBCAM BARCODE SCANNER
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
    echo Compiling enhanced scanner...
    javac -cp "lib/*" -d classes src/main/java/com/example/barcodescanner/EnhancedWebcamScanner.java
    if %errorlevel% neq 0 (
        echo ERROR: Compilation failed
        pause
        exit /b 1
    )
    echo Compilation successful!
)

echo Starting Enhanced Webcam Barcode Scanner...
echo.
echo 🚀 ENHANCED DETECTION FEATURES:
echo ✅ 20 FPS high-speed processing
echo ✅ 3 detection algorithms per frame
echo ✅ Auto image enhancement and preprocessing
echo ✅ 16+ barcode format support
echo ✅ Improved low-light detection
echo ✅ Multiple binarization methods
echo ✅ Real-time diagnostic feedback
echo.
echo INSTRUCTIONS:
echo 1. Click "🎯 Start Enhanced Scanning" in the application
echo 2. Point camera at barcode (6-12 inches away)
echo 3. Keep barcode steady and flat
echo 4. Listen for beep sound when detected
echo 5. See results with detection method used
echo 6. Click "🔍 Run Diagnostic" for detection stats
echo.
echo DETECTION TIPS:
echo • Use good lighting (not too bright/dark)
echo • Hold barcode steady for 2-3 seconds
echo • Try different angles if first attempt fails
echo • Clean camera lens for better clarity
echo • Use high-contrast barcodes when possible
echo.
echo SUPPORTED FORMATS:
echo QR Code, Code 128, Code 39, EAN-13, UPC-A, Data Matrix,
echo PDF417, Aztec, and many more!
echo.

java -cp "classes;lib/*" com.example.barcodescanner.EnhancedWebcamScanner

echo.
echo Enhanced scanner closed.
pause
