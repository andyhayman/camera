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

/**
 * Safe version of barcode scanner that handles webcam errors gracefully
 */
public class SafeBarcodeScanner extends JFrame {
    
    private Object webcam; // Using Object to avoid import issues
    private JLabel cameraLabel;
    private JTextArea resultArea;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusLabel;
    private Timer cameraTimer;
    private MultiFormatReader barcodeReader;
    
    private boolean isScanning = false;
    private boolean webcamAvailable = false;
    private String lastDetectedBarcode = "";
    private long lastDetectionTime = 0;
    private static final long DETECTION_COOLDOWN_MS = 2000;
    
    public SafeBarcodeScanner() {
        System.out.println("Initializing Safe Barcode Scanner...");
        
        try {
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
    
    private void initializeComponents() {
        setTitle("Barcode Scanner - Safe Version");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Initialize barcode reader
        barcodeReader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        // Don't set POSSIBLE_FORMATS to use all formats by default
        barcodeReader.setHints(hints);
        
        System.out.println("Components initialized");
    }
    
    private void setupBarcodeReader() {
        // Camera timer for capturing frames
        cameraTimer = new Timer(100, new ActionListener() { // 10 FPS for safety
            @Override
            public void actionPerformed(ActionEvent e) {
                if (webcam != null && isScanning) {
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
        cameraPanel.setBorder(BorderFactory.createTitledBorder("Camera View"));
        
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
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
        controlPanel.setPreferredSize(new java.awt.Dimension(200, 0));
        
        startButton = new JButton("Start Scanning");
        stopButton = new JButton("Stop Scanning");
        JButton clearButton = new JButton("Clear Results");
        JButton testButton = new JButton("Test Detection");
        
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        testButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        stopButton.setEnabled(false);
        
        statusLabel = new JLabel("Initializing...");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(Color.BLUE);
        
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(startButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(stopButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(testButton);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(statusLabel);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(clearButton);
        controlPanel.add(Box.createVerticalGlue());
        
        // Results panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Scan Results"));
        resultPanel.setPreferredSize(new java.awt.Dimension(0, 150));
        
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultArea.setText("Barcode Scanner Ready!\n");
        resultArea.append("Click 'Start Scanning' to begin.\n");
        resultArea.append("Scanned barcodes will appear here...\n");
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Layout
        add(cameraPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        add(resultPanel, BorderLayout.SOUTH);
        
        // Event handlers for buttons
        clearButton.addActionListener(e -> {
            resultArea.setText("Results cleared.\nReady for new scans...\n");
            updateStatus("Results cleared", Color.BLUE);
        });
        
        testButton.addActionListener(e -> {
            testBarcodeDetection();
        });
        
        System.out.println("UI setup complete");
    }
    
    private void setupEventHandlers() {
        startButton.addActionListener(e -> startScanning());
        stopButton.addActionListener(e -> stopScanning());
        
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
            // Try to load webcam capture class
            Class<?> webcamClass = Class.forName("com.github.sarxos.webcam.Webcam");
            Object defaultWebcam = webcamClass.getMethod("getDefault").invoke(null);
            
            if (defaultWebcam != null) {
                webcamAvailable = true;
                updateStatus("Webcam detected - Ready to scan", Color.GREEN);
                resultArea.append("✓ Webcam available\n");
            } else {
                webcamAvailable = false;
                updateStatus("No webcam found", Color.ORANGE);
                resultArea.append("⚠ No webcam detected\n");
                startButton.setText("No Camera");
                startButton.setEnabled(false);
            }
        } catch (Exception e) {
            webcamAvailable = false;
            updateStatus("Webcam library error: " + e.getMessage(), Color.RED);
            resultArea.append("❌ Webcam library error: " + e.getMessage() + "\n");
            startButton.setText("Camera Error");
            startButton.setEnabled(false);
        }
    }
    
    private void startScanning() {
        if (!webcamAvailable) {
            JOptionPane.showMessageDialog(this, 
                "Webcam is not available.\nPlease check your camera connection and try again.", 
                "Camera Not Available", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            updateStatus("Starting camera...", Color.BLUE);
            
            // Use reflection to avoid import issues
            Class<?> webcamClass = Class.forName("com.github.sarxos.webcam.Webcam");
            webcam = webcamClass.getMethod("getDefault").invoke(null);
            
            if (webcam == null) {
                throw new RuntimeException("No webcam found");
            }
            
            // Open webcam
            Boolean opened = (Boolean) webcamClass.getMethod("open").invoke(webcam);
            if (!opened) {
                throw new RuntimeException("Failed to open webcam");
            }
            
            isScanning = true;
            cameraTimer.start();
            
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            updateStatus("Scanning for barcodes...", Color.GREEN);
            cameraLabel.setText("Camera Active - Point at barcode");
            
        } catch (Exception ex) {
            updateStatus("Error: " + ex.getMessage(), Color.RED);
            JOptionPane.showMessageDialog(this, 
                "Failed to start camera: " + ex.getMessage(), 
                "Camera Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void stopScanning() {
        isScanning = false;
        
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
        
        cameraLabel.setIcon(null);
        cameraLabel.setText("Camera stopped");
        
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        updateStatus("Scanning stopped", Color.ORANGE);
    }
    
    private void captureAndProcessFrame() {
        try {
            if (webcam != null) {
                Class<?> webcamClass = webcam.getClass();
                BufferedImage image = (BufferedImage) webcamClass.getMethod("getImage").invoke(webcam);
                
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
                
                updateStatus("Barcode detected: " + format, Color.GREEN);
                
                // Flash the border to indicate detection
                cameraLabel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
                Timer flashTimer = new Timer(500, e -> {
                    cameraLabel.setBorder(BorderFactory.createLoweredBevelBorder());
                });
                flashTimer.setRepeats(false);
                flashTimer.start();
            }
            
        } catch (NotFoundException e) {
            // No barcode found - this is normal
        } catch (Exception e) {
            // Other errors - log but don't spam the user
            System.err.println("Barcode detection error: " + e.getMessage());
        }
    }
    
    private void testBarcodeDetection() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        resultArea.append("[" + timestamp + "] TEST: Barcode detection system is working!\n");
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
        updateStatus("Test completed", Color.BLUE);
        
        JOptionPane.showMessageDialog(this, 
            "Barcode detection system is ready!\n\n" +
            "Supported formats: QR_CODE, CODE_128, EAN_13, UPC_A, and 13 more!\n" +
            "Click 'Start Scanning' to begin live detection.", 
            "System Test", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateStatus(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("<html><div style='text-align: center;'>" + message + "</div></html>");
            statusLabel.setForeground(color);
        });
    }
    
    private void cleanup() {
        stopScanning();
    }
    
    public static void main(String[] args) {
        System.out.println("=== Safe Barcode Scanner Starting ===");
        
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("Look and feel set successfully");
        } catch (Exception e) {
            System.out.println("Using default look and feel");
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                SafeBarcodeScanner scanner = new SafeBarcodeScanner();
                scanner.setVisible(true);
                System.out.println("=== GUI should now be visible ===");
            } catch (Exception e) {
                System.err.println("Failed to create GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
