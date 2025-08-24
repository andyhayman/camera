# Barcode Scanner Application

A Java application that allows users to scan barcodes using their computer's camera and display the decoded values in real-time.

## Features

- **Real-time barcode scanning** using computer webcam
- **Multiple barcode format support** including QR codes, Code 128, Code 39, EAN-13, UPC-A, and more
- **Live camera preview** with barcode detection overlay
- **Scan results display** with timestamp and barcode format
- **User-friendly GUI** built with JavaFX
- **Error handling** for camera access and scanning issues

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- A computer with a webcam
- Windows, macOS, or Linux operating system

## Installation and Setup

1. **Clone or download** this project to your local machine

2. **Navigate to the project directory**:
   ```bash
   cd camera
   ```

3. **Build the project** using Maven:
   ```bash
   mvn clean compile
   ```

4. **Download dependencies**:
   ```bash
   mvn dependency:resolve
   ```

## Running the Application

### Option 1: Using Maven (Recommended if Maven is installed)
```bash
mvn javafx:run
```

### Option 2: Using Maven Exec Plugin
```bash
mvn exec:java -Dexec.mainClass="com.example.barcodescanner.BarcodeScanner"
```

### Option 3: Manual Compilation (if Maven is not available)

1. **Download required dependencies** and place them in a `lib` folder:
   - JavaFX Controls 17.0.2+ (javafx-controls-17.0.2.jar)
   - JavaFX FXML 17.0.2+ (javafx-fxml-17.0.2.jar)
   - ZXing Core 3.5.1+ (core-3.5.1.jar)
   - ZXing JavaSE 3.5.1+ (javase-3.5.1.jar)
   - Webcam Capture 0.3.12+ (webcam-capture-0.3.12.jar)

2. **Compile the application**:
   ```bash
   compile.bat    # On Windows
   ```

3. **Run the application**:
   ```bash
   run.bat        # On Windows
   ```

### Option 4: Building JAR with Maven
```bash
mvn clean package
java -jar target/barcode-scanner-1.0.0.jar
```

## How to Use

1. **Launch the application** using one of the methods above
2. **Click "Start Scanning"** to activate your camera
3. **Point your camera** at a barcode or QR code
4. **View results** in the results panel at the bottom
5. **Click "Stop Scanning"** when finished
6. **Click "Clear Results"** to clear the results area

## Supported Barcode Formats

- QR Code
- Code 128
- Code 39
- Code 93
- EAN-13
- EAN-8
- UPC-A
- UPC-E
- Codabar
- ITF (Interleaved 2 of 5)
- RSS-14
- RSS-Expanded
- Data Matrix
- PDF417
- Aztec

## Manual Dependency Download

If you don't have Maven installed, you can download the required JAR files manually:

1. **JavaFX** (from https://openjfx.io/):
   - javafx-controls-17.0.2.jar
   - javafx-fxml-17.0.2.jar

2. **ZXing** (from https://repo1.maven.org/maven2/com/google/zxing/):
   - core-3.5.1.jar (from com/google/zxing/core/3.5.1/)
   - javase-3.5.1.jar (from com/google/zxing/javase/3.5.1/)

3. **Webcam Capture** (from https://repo1.maven.org/maven2/com/github/sarxos/):
   - webcam-capture-0.3.12.jar (from com/github/sarxos/webcam-capture/0.3.12/)

Place all JAR files in a `lib` directory in the project root, then use `compile.bat` and `run.bat`.

## Troubleshooting

### Camera Not Found
- Ensure your webcam is connected and not being used by another application
- Check that your webcam drivers are properly installed
- Try restarting the application

### Permission Issues
- On some systems, you may need to grant camera permissions to Java applications
- Check your system's privacy settings for camera access

### Build Issues
- Ensure you have Java 11+ installed
- If using Maven: ensure Maven 3.6+ is installed and run `mvn clean` before building
- If compiling manually: ensure all required JAR files are in the `lib` directory
- Check that your JAVA_HOME environment variable is set correctly

## Dependencies

- **JavaFX 17.0.2** - For the graphical user interface
- **ZXing 3.5.1** - For barcode detection and decoding
- **Webcam Capture 0.3.12** - For camera access and video capture

## Project Structure

```
src/
├── main/
│   ├── java/com/example/barcodescanner/
│   │   ├── BarcodeScanner.java      # Main application class
│   │   ├── CameraService.java       # Camera management service
│   │   └── BarcodeDetector.java     # Barcode detection service
│   └── resources/                   # Application resources
└── test/
    └── java/                        # Unit tests
```

## License

This project is open source and available under the MIT License.
