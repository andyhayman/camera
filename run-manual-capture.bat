@echo off
echo ==========================================
echo    MANUAL CAPTURE BARCODE SCANNER
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
    echo Compiling manual capture scanner...
    javac -cp "lib/*" -d classes src/main/java/com/example/barcodescanner/ManualCaptureScanner.java
    if %errorlevel% neq 0 (
        echo Compilation failed!
        pause
        exit /b 1
    )
)

echo.
echo Starting Manual Capture Barcode Scanner...
echo.

REM Run with optimized settings for barcode detection
java --enable-native-access=ALL-UNNAMED ^
     -Dwebcam.debug=true ^
     -Dwebcam.device.resolution.high=true ^
     -Dwebcam.capture.driver=bridj ^
     -Dwebcam.device.resolution.min.width=1280 ^
     -Dwebcam.device.resolution.min.height=720 ^
     -Dzxing.tryHarder=true ^
     -cp "classes;lib/*" ^
     com.example.barcodescanner.ManualCaptureScanner

echo.
pause
