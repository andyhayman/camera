@echo off
echo Starting Barcode Scanner Application...
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
    echo Classes directory not found. Please compile first using compile.bat
    pause
    exit /b 1
)

REM Check if lib directory has JAR files
set JAR_COUNT=0
for %%f in (lib\*.jar) do set /a JAR_COUNT+=1

if %JAR_COUNT% equ 0 (
    echo No JAR files found in lib directory.
    echo Please download dependencies and compile using compile.bat
    pause
    exit /b 1
)

echo Starting application...
java -cp "classes;lib\*" com.example.barcodescanner.BarcodeScanner

pause
