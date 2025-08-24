@echo off
echo ==========================================
echo    HYBRID BARCODE SCANNER - BEST VERSION
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
    echo Compiling hybrid application...
    javac -cp "lib/*" -d classes src/main/java/com/example/barcodescanner/HybridBarcodeScanner.java
    if %errorlevel% neq 0 (
        echo ERROR: Compilation failed
        pause
        exit /b 1
    )
    echo Compilation successful!
)

echo Starting Hybrid Barcode Scanner...
echo.
echo FEATURES:
echo ✅ WEBCAM MODE: Live camera scanning (if camera available)
echo ✅ IMAGE MODE: Scan barcode image files (always works)
echo ✅ 17+ barcode formats supported
echo ✅ Automatic fallback if camera fails
echo.
echo INSTRUCTIONS:
echo 1. Choose your scanning mode in the application
echo 2. WEBCAM: Click "Start Webcam" for live scanning
echo 3. IMAGE: Click "Load Image File" for file-based scanning
echo 4. Results appear automatically with sound notification
echo.
echo TIP: If webcam doesn't work, Image mode always works!
echo      Take a screenshot of a QR code and load it!
echo.

java -cp "classes;lib/*" com.example.barcodescanner.HybridBarcodeScanner

echo.
echo Hybrid scanner closed.
pause
