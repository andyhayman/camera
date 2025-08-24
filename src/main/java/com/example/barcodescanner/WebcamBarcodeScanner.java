package com.example.barcodescanner;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.reflect.Method;

/**
 * Webcam-enabled barcode scanner with improved error handling
 */
public class WebcamBarcodeScanner extends JFrame {
    
    private Object webcam; // Using Object to avoid direct dependency
    private JLabel cameraLabel;
    private JTextArea resultArea;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusLabel;
    private Timer cameraTimer;
    private MultiFormatReader barcodeReader;
    
    private final AtomicBoolean isScanning = new AtomicBoolean(false);
    private boolean webcamAvailable = false;
    private String lastDetectedBarcode = "";
    private long lastDetectionTime = 0;
    private static final long DETECTION_COOLDOWN_MS = 2000;
    
    // Reflection methods for webcam operations
    private Method webcamGetDefaultMethod;
    private Method webcamOpenMethod;
    private Method webcamCloseMethod;
    private Method webcamGetImageMethod;
    private Method webcamIsOpenMethod;
    private Method webcamSetViewSizeMethod;
    private Method webcamGetViewSizesMethod;
    
    public WebcamBarcodeScanner() {
        System.out.println("Initializing Webcam Barcode Scanner...");
        
        try {
            initializeWebcamReflection();
            initializeComponents();
            setupBarcodeReader();
            setupUI();
            setupEventHandlers();
            checkWebcamAvailability();
            
            System.out.println("GUI initialization complete!");
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeWebcamReflection() {
        try {
            Class<?> webcamClass = Class.forName("com.github.sarxos.webcam.Webcam");
            webcamGetDefaultMethod = webcamClass.getMethod("getDefault");
            webcamOpenMethod = webcamClass.getMethod("open");
            webcamCloseMethod = webcamClass.getMethod("close");
            webcamGetImageMethod = webcamClass.getMethod("getImage");
            webcamIsOpenMethod = webcamClass.getMethod("isOpen");
            webcamSetViewSizeMethod = webcamClass.getMethod("setViewSize", java.awt.Dimension.class);
            webcamGetViewSizesMethod = webcamClass.getMethod("getViewSizes");
            
            System.out.println("Webcam reflection methods initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize webcam reflection: " + e.getMessage());
            throw new RuntimeException("Webcam library not available", e);
        }
    }
    
    private void initializeComponents() {
        setTitle("Webcam Barcode Scanner - Live Camera");
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
        cameraTimer = new Timer(33, new ActionListener() { // ~30 FPS
            @Override
            public void actionPerformed(ActionEvent e) {
                if (webcam != null && isScanning.get()) {
                    try {
                        captureAndProcessFrame();
                    } catch (Exception ex) {
                        updateStatus("Error processing frame: " + ex.getMessage(), Color.RED);
                    }
                }
            }
        });
        
        System.out.println("Barcode reader setup complete");
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Camera panel
        JPanel cameraPanel = new JPanel(new BorderLayout());
        cameraPanel.setBorder(BorderFactory.createTitledBorder("Live Camera Feed"));
        
        cameraLabel = new JLabel("Camera not started", SwingConstants.CENTER);
        cameraLabel.setPreferredSize(new java.awt.Dimension(640, 480));
        cameraLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        cameraLabel.setBackground(Color.BLACK);
        cameraLabel.setOpaque(true);
        cameraLabel.setForeground(Color.WHITE);
        cameraLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        cameraPanel.add(cameraLabel, BorderLayout.CENTER);
        
        // Control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Camera Controls"));
        controlPanel.setPreferredSize(new java.awt.Dimension(200, 0));
        
        startButton = new JButton("Start Camera");
        stopButton = new JButton("Stop Camera");
        JButton clearButton = new JButton("Clear Results");
        JButton helpButton = new JButton("Help");
        
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        helpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        stopButton.setEnabled(false);
        
        statusLabel = new JLabel("Checking camera...");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(Color.BLUE);
        
        // Info labels
        JLabel infoLabel1 = new JLabel("Point camera at barcode");
        JLabel infoLabel2 = new JLabel("Detection is automatic");
        infoLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoLabel1.setFont(new Font("Arial", Font.PLAIN, 10));
        infoLabel2.setFont(new Font("Arial", Font.PLAIN, 10));
        
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(startButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(stopButton);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(statusLabel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(infoLabel1);
        controlPanel.add(infoLabel2);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(clearButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(helpButton);
        controlPanel.add(Box.createVerticalGlue());
        
        // Results panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Live Scan Results"));
        resultPanel.setPreferredSize(new java.awt.Dimension(0, 150));
        
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultArea.setText("Webcam Barcode Scanner Ready!\n");
        resultArea.append("Click 'Start Camera' to begin live scanning.\n");
        resultArea.append("Point your camera at any barcode for automatic detection.\n\n");
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Layout
        add(cameraPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        add(resultPanel, BorderLayout.SOUTH);
        
        // Event handlers for buttons
        clearButton.addActionListener(e -> {
            resultArea.setText("Results cleared.\nReady for live scanning...\n");
            updateStatus("Results cleared", Color.BLUE);
        });
        
        helpButton.addActionListener(e -> showHelp());
        
        System.out.println("UI setup complete");
    }
    
    private void setupEventHandlers() {
        startButton.addActionListener(e -> startCamera());
        stopButton.addActionListener(e -> stopCamera());
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
        
        System.out.println("Event handlers setup complete");
    }
    
    private void checkWebcamAvailability() {
        try {
            webcam = webcamGetDefaultMethod.invoke(null);
            
            if (webcam != null) {
                webcamAvailable = true;
                updateStatus("Camera detected - Ready to start", Color.GREEN);
                resultArea.append("✓ Camera available and ready\n");
            } else {
                webcamAvailable = false;
                updateStatus("No camera found", Color.ORANGE);
                resultArea.append("⚠ No camera detected\n");
                startButton.setText("No Camera");
                startButton.setEnabled(false);
            }
        } catch (Exception e) {
            webcamAvailable = false;
            updateStatus("Camera error: " + e.getMessage(), Color.RED);
            resultArea.append("❌ Camera error: " + e.getMessage() + "\n");
            startButton.setText("Camera Error");
            startButton.setEnabled(false);
        }
    }
    
    private void startCamera() {
        if (!webcamAvailable) {
            JOptionPane.showMessageDialog(this, 
                "Camera is not available.\nPlease check your camera connection and permissions.", 
                "Camera Not Available", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            updateStatus("Starting camera...", Color.BLUE);
            
            if (webcam == null) {
                throw new RuntimeException("No camera found");
            }
            
            // Set camera resolution
            java.awt.Dimension[] sizes = (java.awt.Dimension[]) webcamGetViewSizesMethod.invoke(webcam);
            java.awt.Dimension selectedSize = new java.awt.Dimension(640, 480);
            
            // Try to find 640x480 resolution
            for (java.awt.Dimension size : sizes) {
                if (size.width == 640 && size.height == 480) {
                    selectedSize = size;
                    break;
                }
            }
            
            webcamSetViewSizeMethod.invoke(webcam, selectedSize);
            
            // Open camera
            Boolean opened = (Boolean) webcamOpenMethod.invoke(webcam);
            if (!opened) {
                throw new RuntimeException("Failed to open camera");
            }
            
            isScanning.set(true);
            cameraTimer.start();
            
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            updateStatus("Camera active - Scanning for barcodes...", Color.GREEN);
            cameraLabel.setText("Camera starting...");
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            resultArea.append("[" + timestamp + "] Camera started - Live scanning active\n");
            
        } catch (Exception ex) {
            updateStatus("Error: " + ex.getMessage(), Color.RED);
            JOptionPane.showMessageDialog(this, 
                "Failed to start camera: " + ex.getMessage() + 
                "\n\nPossible solutions:\n" +
                "• Close other applications using the camera\n" +
                "• Check camera permissions in Windows settings\n" +
                "• Try restarting the application", 
                "Camera Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void stopCamera() {
        isScanning.set(false);
        
        if (cameraTimer != null) {
            cameraTimer.stop();
        }
        
        if (webcam != null) {
            try {
                Boolean isOpen = (Boolean) webcamIsOpenMethod.invoke(webcam);
                if (isOpen) {
                    webcamCloseMethod.invoke(webcam);
                }
            } catch (Exception e) {
                System.err.println("Error closing camera: " + e.getMessage());
            }
        }
        
        cameraLabel.setIcon(null);
        cameraLabel.setText("Camera stopped");
        
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        updateStatus("Camera stopped", Color.ORANGE);
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        resultArea.append("[" + timestamp + "] Camera stopped\n");
    }
    
    private void captureAndProcessFrame() {
        try {
            if (webcam != null) {
                BufferedImage image = (BufferedImage) webcamGetImageMethod.invoke(webcam);
                
                if (image != null) {
                    // Update camera display
                    ImageIcon icon = new ImageIcon(image.getScaledInstance(640, 480, Image.SCALE_FAST));
                    cameraLabel.setIcon(icon);
                    cameraLabel.setText("");
                    
                    // Try to detect barcode
                    detectBarcode(image);
                }
            }
        } catch (Exception e) {
            updateStatus("Frame capture error: " + e.getMessage(), Color.RED);
        }
    }
    
    private void detectBarcode(BufferedImage image) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
            Result result = barcodeReader.decode(bitmap);
            String barcodeText = result.getText();
            String format = result.getBarcodeFormat().toString();
            
            // Avoid duplicate detections
            long currentTime = System.currentTimeMillis();
            if (!barcodeText.equals(lastDetectedBarcode) || 
                (currentTime - lastDetectionTime) > DETECTION_COOLDOWN_MS) {
                
                lastDetectedBarcode = barcodeText;
                lastDetectionTime = currentTime;
                
                // Add to results
                String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("HH:mm:ss")
                );
                String resultText = String.format("[%s] %s: %s\n", timestamp, format, barcodeText);
                resultArea.append(resultText);
                resultArea.setCaretPosition(resultArea.getDocument().getLength());
                
                updateStatus("DETECTED: " + format, Color.GREEN);
                
                // Flash the border to indicate detection
                cameraLabel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 5));
                Timer flashTimer = new Timer(1000, e -> {
                    cameraLabel.setBorder(BorderFactory.createLoweredBevelBorder());
                });
                flashTimer.setRepeats(false);
                flashTimer.start();
                
                // Play system beep
                Toolkit.getDefaultToolkit().beep();
            }
            
        } catch (NotFoundException e) {
            // No barcode found - this is normal
        } catch (Exception e) {
            // Other errors - log but don't spam the user
            System.err.println("Barcode detection error: " + e.getMessage());
        }
    }
    
    private void showHelp() {
        String helpText = "Webcam Barcode Scanner Help\n\n" +
                         "How to use:\n" +
                         "1. Click 'Start Camera' to activate your webcam\n" +
                         "2. Point the camera at any barcode or QR code\n" +
                         "3. Detection happens automatically - no need to click anything!\n" +
                         "4. Results appear instantly in the bottom panel\n" +
                         "5. Click 'Stop Camera' when finished\n\n" +
                         "Supported barcode formats:\n" +
                         "• QR Code, Code 128, Code 39, EAN-13, UPC-A\n" +
                         "• Data Matrix, PDF417, and 10+ more formats\n\n" +
                         "Tips for best results:\n" +
                         "• Ensure good lighting\n" +
                         "• Hold barcode steady (6-12 inches from camera)\n" +
                         "• Keep barcode flat and unrotated\n" +
                         "• Use high-contrast barcodes (black on white)\n\n" +
                         "Troubleshooting:\n" +
                         "• If camera won't start, close other camera apps\n" +
                         "• Check Windows camera permissions\n" +
                         "• Try restarting the application";
        
        JOptionPane.showMessageDialog(this, helpText, "Help", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateStatus(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("<html><div style='text-align: center;'>" + message + "</div></html>");
            statusLabel.setForeground(color);
        });
    }
    
    private void cleanup() {
        stopCamera();
    }
    
    public static void main(String[] args) {
        System.out.println("=== Webcam Barcode Scanner Starting ===");
        
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("Look and feel set successfully");
        } catch (Exception e) {
            System.out.println("Using default look and feel");
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                WebcamBarcodeScanner scanner = new WebcamBarcodeScanner();
                scanner.setVisible(true);
                System.out.println("=== Webcam GUI should now be visible ===");
            } catch (Exception e) {
                System.err.println("Failed to create GUI: " + e.getMessage());
                e.printStackTrace();
                
                // Fallback message
                JOptionPane.showMessageDialog(null, 
                    "Failed to start webcam scanner: " + e.getMessage() + 
                    "\n\nThis might be due to missing camera drivers or permissions." +
                    "\n\nTry running the image-file version instead: run-working-gui.bat", 
                    "Webcam Scanner Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
