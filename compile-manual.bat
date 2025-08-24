@echo off
echo Compiling Manual Capture Scanner...
echo.

REM Create classes directory if it doesn't exist
if not exist "classes" mkdir classes

REM Compile all required files
javac -cp "lib/*" -d classes ^
    src/main/java/com/example/barcodescanner/ManualCaptureScanner.java

if %errorlevel% neq 0 (
    echo.
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo.
pause
