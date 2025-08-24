@echo off
echo ==========================================
echo    ENHANCED WEBCAM BARCODE SCANNER (FIXED)
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
    echo Compiling enhanced scanner application...
    javac -cp "lib/*" -d classes src/main/java/com/example/barcodescanner/EnhancedWebcamScanner.java
    if %errorlevel% neq 0 (
        echo Compilation failed!
        pause
        exit /b 1
    )
)

echo.
echo Starting Enhanced Webcam Barcode Scanner...
echo.

REM Run with native access enabled and system properties for webcam
java --enable-native-access=ALL-UNNAMED ^
     -Dwebcam.debug=true ^
     -Dcom.github.sarxos.webcam.capture.driver=gstreamer ^
     -Dwebcam.device.resolution.min.width=640 ^
     -Dwebcam.device.resolution.min.height=480 ^
     -cp "classes;lib/*" ^
     com.example.barcodescanner.EnhancedWebcamScanner

echo.
pause
