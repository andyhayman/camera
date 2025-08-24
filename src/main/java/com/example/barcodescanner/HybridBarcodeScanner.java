package com.example.barcodescanner;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Hybrid barcode scanner that works with both webcam and image files
 * Falls back to image mode if webcam is not available
 */
public class HybridBarcodeScanner extends JFrame {
    
    private Object webcam;
    private JLabel displayLabel;
    private JTextArea resultArea;
    private JButton webcamButton;
    private JButton imageButton;
    private JButton stopButton;
    private JLabel statusLabel;
    private Timer cameraTimer;
    private MultiFormatReader barcodeReader;
    private BufferedImage currentImage;
    
    private boolean isWebcamMode = false;
    private boolean isScanning = false;
    private boolean webcamAvailable = false;
    private String lastDetectedBarcode = "";
    private long lastDetectionTime = 0;
    private static final long DETECTION_COOLDOWN_MS = 2000;
    
    public HybridBarcodeScanner() {
        System.out.println("Initializing Hybrid Barcode Scanner...");
        
        initializeComponents();
        setupBarcodeReader();
        setupUI();
        setupEventHandlers();
        checkWebcamAvailability();
        
        System.out.println("GUI initialization complete!");
    }
    
    private void initializeComponents() {
        setTitle("Barcode Scanner - Webcam + Image Files");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Initialize barcode reader
        barcodeReader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        barcodeReader.setHints(hints);
        
        System.out.println("Components initialized");
    }
    
    private void setupBarcodeReader() {
        // Camera timer for capturing frames
        cameraTimer = new Timer(100, new ActionListener() { // 10 FPS for stability
            @Override
            public void actionPerformed(ActionEvent e) {
                if (webcam != null && isScanning && isWebcamMode) {
                    try {
                        captureAndProcessFrame();
                    } catch (Exception ex) {
                        updateStatus("Frame error: " + ex.getMessage(), Color.RED);
                    }
                }
            }
        });
        
        System.out.println("Barcode reader setup complete");
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Display panel
        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBorder(BorderFactory.createTitledBorder("Barcode Scanner Display"));
        
        displayLabel = new JLabel("Select scanning mode below", SwingConstants.CENTER);
        displayLabel.setPreferredSize(new java.awt.Dimension(640, 480));
        displayLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        displayLabel.setBackground(Color.LIGHT_GRAY);
        displayLabel.setOpaque(true);
        displayLabel.setForeground(Color.DARK_GRAY);
        displayLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        displayPanel.add(displayLabel, BorderLayout.CENTER);
        
        // Control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Scanner Controls"));
        controlPanel.setPreferredSize(new java.awt.Dimension(220, 0));
        
        // Mode selection
        JLabel modeLabel = new JLabel("Scanning Mode:");
        modeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        modeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        webcamButton = new JButton("📹 Start Webcam");
        imageButton = new JButton("📁 Load Image File");
        stopButton = new JButton("⏹ Stop Scanning");
        JButton clearButton = new JButton("🗑 Clear Results");
        JButton helpButton = new JButton("❓ Help");
        
        webcamButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        helpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        stopButton.setEnabled(false);
        
        statusLabel = new JLabel("Ready - Choose scanning mode");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(Color.BLUE);
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Info"));
        
        JLabel info1 = new JLabel("Webcam: Live scanning");
        JLabel info2 = new JLabel("Image: File-based scanning");
        JLabel info3 = new JLabel("Supports 17+ formats");
        
        info1.setAlignmentX(Component.CENTER_ALIGNMENT);
        info2.setAlignmentX(Component.CENTER_ALIGNMENT);
        info3.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        info1.setFont(new Font("Arial", Font.PLAIN, 10));
        info2.setFont(new Font("Arial", Font.PLAIN, 10));
        info3.setFont(new Font("Arial", Font.PLAIN, 10));
        
        infoPanel.add(info1);
        infoPanel.add(info2);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(info3);
        
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(modeLabel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(webcamButton);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(imageButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(stopButton);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(statusLabel);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(infoPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(clearButton);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(helpButton);
        controlPanel.add(Box.createVerticalGlue());
        
        // Results panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Scan Results"));
        resultPanel.setPreferredSize(new java.awt.Dimension(0, 150));
        
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultArea.setText("🎯 Hybrid Barcode Scanner Ready!\n\n");
        resultArea.append("📹 WEBCAM MODE: Live camera scanning\n");
        resultArea.append("📁 IMAGE MODE: Scan barcode image files\n\n");
        resultArea.append("Supported formats: QR Code, Code 128, EAN-13, UPC-A, and more!\n");
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Layout
        add(displayPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        add(resultPanel, BorderLayout.SOUTH);
        
        // Event handlers for buttons
        clearButton.addActionListener(e -> {
            resultArea.setText("Results cleared.\nReady for scanning...\n");
            updateStatus("Results cleared", Color.BLUE);
        });
        
        helpButton.addActionListener(e -> showHelp());
        
        System.out.println("UI setup complete");
    }
    
    private void setupEventHandlers() {
        webcamButton.addActionListener(e -> startWebcamMode());
        imageButton.addActionListener(e -> loadImageFile());
        stopButton.addActionListener(e -> stopScanning());
        
        System.out.println("Event handlers setup complete");
    }
    
    private void checkWebcamAvailability() {
        try {
            Class<?> webcamClass = Class.forName("com.github.sarxos.webcam.Webcam");
            webcam = webcamClass.getMethod("getDefault").invoke(null);
            
            if (webcam != null) {
                webcamAvailable = true;
                updateStatus("Camera detected - Both modes available", Color.GREEN);
                resultArea.append("✅ Webcam available\n");
            } else {
                webcamAvailable = false;
                updateStatus("No camera - Image mode only", Color.ORANGE);
                resultArea.append("⚠️ No webcam detected - Image mode available\n");
                webcamButton.setText("📹 No Camera Found");
                webcamButton.setEnabled(false);
            }
        } catch (Exception e) {
            webcamAvailable = false;
            updateStatus("Camera error - Image mode only", Color.ORANGE);
            resultArea.append("❌ Camera error: " + e.getMessage() + "\n");
            resultArea.append("📁 Image file mode is still available\n");
            webcamButton.setText("📹 Camera Error");
            webcamButton.setEnabled(false);
        }
    }
    
    private void startWebcamMode() {
        if (!webcamAvailable) {
            JOptionPane.showMessageDialog(this, 
                "Webcam is not available.\n\nYou can still use Image File mode:\n" +
                "1. Click 'Load Image File'\n" +
                "2. Select an image containing a barcode\n" +
                "3. The barcode will be detected automatically", 
                "Webcam Not Available", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            updateStatus("Starting webcam...", Color.BLUE);
            
            if (webcam == null) {
                throw new RuntimeException("No webcam found");
            }
            
            // Open webcam using reflection
            Class<?> webcamClass = webcam.getClass();
            Boolean opened = (Boolean) webcamClass.getMethod("open").invoke(webcam);
            
            if (!opened) {
                throw new RuntimeException("Failed to open webcam");
            }
            
            isWebcamMode = true;
            isScanning = true;
            cameraTimer.start();
            
            webcamButton.setEnabled(false);
            imageButton.setEnabled(false);
            stopButton.setEnabled(true);
            
            updateStatus("Webcam active - Point at barcode", Color.GREEN);
            displayLabel.setText("Webcam starting...");
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            resultArea.append("[" + timestamp + "] 📹 Webcam mode started\n");
            
        } catch (Exception ex) {
            updateStatus("Webcam error: " + ex.getMessage(), Color.RED);
            JOptionPane.showMessageDialog(this, 
                "Failed to start webcam: " + ex.getMessage() + 
                "\n\nTry Image File mode instead:\n" +
                "• Click 'Load Image File'\n" +
                "• Select a barcode image\n" +
                "• Works with screenshots, photos, etc.", 
                "Webcam Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadImageFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Barcode Image");
        
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image files", "jpg", "jpeg", "png", "gif", "bmp");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            try {
                currentImage = ImageIO.read(selectedFile);
                if (currentImage != null) {
                    // Display image
                    ImageIcon icon = new ImageIcon(currentImage.getScaledInstance(640, 480, Image.SCALE_SMOOTH));
                    displayLabel.setIcon(icon);
                    displayLabel.setText("");
                    
                    updateStatus("Image loaded - Scanning...", Color.BLUE);
                    
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    resultArea.append("[" + timestamp + "] 📁 Image loaded: " + selectedFile.getName() + "\n");
                    
                    // Automatically scan the loaded image
                    scanCurrentImage();
                    
                } else {
                    throw new Exception("Could not read image file");
                }
            } catch (Exception ex) {
                updateStatus("Error loading image: " + ex.getMessage(), Color.RED);
                JOptionPane.showMessageDialog(this, 
                    "Failed to load image: " + ex.getMessage(), 
                    "Image Load Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void scanCurrentImage() {
        if (currentImage == null) return;
        
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(currentImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
            Result result = barcodeReader.decode(bitmap);
            String barcodeText = result.getText();
            String format = result.getBarcodeFormat().toString();
            
            // Add to results
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String resultText = String.format("[%s] %s: %s\n", timestamp, format, barcodeText);
            resultArea.append(resultText);
            resultArea.setCaretPosition(resultArea.getDocument().getLength());
            
            updateStatus("DETECTED: " + format, Color.GREEN);
            
            // Flash the border
            displayLabel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 5));
            Timer flashTimer = new Timer(1000, e -> {
                displayLabel.setBorder(BorderFactory.createLoweredBevelBorder());
            });
            flashTimer.setRepeats(false);
            flashTimer.start();
            
            // Show success dialog
            JOptionPane.showMessageDialog(this, 
                "✅ Barcode detected successfully!\n\n" +
                "Format: " + format + "\n" +
                "Content: " + barcodeText, 
                "Barcode Detected", 
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NotFoundException e) {
            updateStatus("No barcode found in image", Color.ORANGE);
            resultArea.append("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + 
                             "] ❌ No barcode detected in image\n");
            
            JOptionPane.showMessageDialog(this, 
                "No barcode found in the image.\n\n" +
                "Tips:\n" +
                "• Ensure the image contains a clear barcode\n" +
                "• Try a higher resolution image\n" +
                "• Make sure the barcode is not rotated", 
                "No Barcode Found", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void captureAndProcessFrame() {
        try {
            if (webcam != null) {
                Class<?> webcamClass = webcam.getClass();
                BufferedImage image = (BufferedImage) webcamClass.getMethod("getImage").invoke(webcam);
                
                if (image != null) {
                    // Update display
                    ImageIcon icon = new ImageIcon(image.getScaledInstance(640, 480, Image.SCALE_FAST));
                    displayLabel.setIcon(icon);
                    displayLabel.setText("");
                    
                    // Try to detect barcode
                    detectBarcodeInFrame(image);
                }
            }
        } catch (Exception e) {
            updateStatus("Frame capture error", Color.RED);
        }
    }
    
    private void detectBarcodeInFrame(BufferedImage image) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
            Result result = barcodeReader.decode(bitmap);
            String barcodeText = result.getText();
            String format = result.getBarcodeFormat().toString();
            
            // Avoid duplicates
            long currentTime = System.currentTimeMillis();
            if (!barcodeText.equals(lastDetectedBarcode) || 
                (currentTime - lastDetectionTime) > DETECTION_COOLDOWN_MS) {
                
                lastDetectedBarcode = barcodeText;
                lastDetectionTime = currentTime;
                
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                String resultText = String.format("[%s] %s: %s\n", timestamp, format, barcodeText);
                resultArea.append(resultText);
                resultArea.setCaretPosition(resultArea.getDocument().getLength());
                
                updateStatus("DETECTED: " + format, Color.GREEN);
                
                // Visual feedback
                displayLabel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 5));
                Timer flashTimer = new Timer(1000, e -> {
                    displayLabel.setBorder(BorderFactory.createLoweredBevelBorder());
                });
                flashTimer.setRepeats(false);
                flashTimer.start();
                
                // Sound feedback
                Toolkit.getDefaultToolkit().beep();
            }
            
        } catch (NotFoundException e) {
            // No barcode found - normal
        }
    }
    
    private void stopScanning() {
        isScanning = false;
        isWebcamMode = false;
        
        if (cameraTimer != null) {
            cameraTimer.stop();
        }
        
        if (webcam != null) {
            try {
                Class<?> webcamClass = webcam.getClass();
                webcamClass.getMethod("close").invoke(webcam);
            } catch (Exception e) {
                System.err.println("Error closing webcam: " + e.getMessage());
            }
        }
        
        displayLabel.setIcon(null);
        displayLabel.setText("Scanning stopped - Select mode below");
        
        webcamButton.setEnabled(webcamAvailable);
        imageButton.setEnabled(true);
        stopButton.setEnabled(false);
        
        updateStatus("Scanning stopped", Color.ORANGE);
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        resultArea.append("[" + timestamp + "] ⏹ Scanning stopped\n");
    }
    
    private void showHelp() {
        String helpText = "🎯 Hybrid Barcode Scanner Help\n\n" +
                         "📹 WEBCAM MODE:\n" +
                         "• Click 'Start Webcam' to use live camera\n" +
                         "• Point camera at barcode for automatic detection\n" +
                         "• Works with real-time video feed\n\n" +
                         "📁 IMAGE FILE MODE:\n" +
                         "• Click 'Load Image File' to scan image files\n" +
                         "• Supports JPG, PNG, GIF, BMP formats\n" +
                         "• Perfect for screenshots and photos\n\n" +
                         "✅ SUPPORTED FORMATS:\n" +
                         "QR Code, Code 128, Code 39, EAN-13, UPC-A,\n" +
                         "Data Matrix, PDF417, and 10+ more!\n\n" +
                         "💡 TIPS:\n" +
                         "• Ensure good lighting for webcam\n" +
                         "• Use high-resolution images\n" +
                         "• Keep barcodes flat and unrotated\n" +
                         "• Try both modes if one doesn't work";
        
        JOptionPane.showMessageDialog(this, helpText, "Help", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateStatus(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("<html><div style='text-align: center;'>" + message + "</div></html>");
            statusLabel.setForeground(color);
        });
    }
    
    public static void main(String[] args) {
        System.out.println("=== Hybrid Barcode Scanner Starting ===");
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                HybridBarcodeScanner scanner = new HybridBarcodeScanner();
                scanner.setVisible(true);
                System.out.println("=== Hybrid GUI should now be visible ===");
            } catch (Exception e) {
                System.err.println("Failed to create GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
