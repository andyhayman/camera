package com.example.barcodescanner;

import com.github.sarxos.webcam.Webcam;
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

/**
 * Working webcam barcode scanner with fixed dependencies
 */
public class WorkingWebcamScanner extends JFrame {
    
    private Webcam webcam;
    private JLabel cameraLabel;
    private JTextArea resultArea;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusLabel;
    private Timer cameraTimer;
    private MultiFormatReader barcodeReader;
    
    private final AtomicBoolean isScanning = new AtomicBoolean(false);
    private String lastDetectedBarcode = "";
    private long lastDetectionTime = 0;
    private static final long DETECTION_COOLDOWN_MS = 2000;
    
    public WorkingWebcamScanner() {
        System.out.println("Initializing Working Webcam Barcode Scanner...");
        
        initializeComponents();
        setupBarcodeReader();
        setupUI();
        setupEventHandlers();
        checkWebcamAvailability();
        
        System.out.println("GUI initialization complete!");
    }
    
    private void initializeComponents() {
        setTitle("🎯 Working Webcam Barcode Scanner");
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
                if (webcam != null && webcam.isOpen() && isScanning.get()) {
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
        
        // Camera panel
        JPanel cameraPanel = new JPanel(new BorderLayout());
        cameraPanel.setBorder(BorderFactory.createTitledBorder("📹 Live Camera Feed"));
        
        cameraLabel = new JLabel("Camera ready - Click Start to begin", SwingConstants.CENTER);
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
        controlPanel.setBorder(BorderFactory.createTitledBorder("🎮 Camera Controls"));
        controlPanel.setPreferredSize(new java.awt.Dimension(220, 0));
        
        startButton = new JButton("🚀 Start Live Scanning");
        stopButton = new JButton("⏹ Stop Scanning");
        JButton clearButton = new JButton("🗑 Clear Results");
        JButton helpButton = new JButton("❓ Help");
        
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        helpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        stopButton.setEnabled(false);
        
        statusLabel = new JLabel("Ready to scan");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(Color.GREEN);
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("ℹ️ Info"));
        
        JLabel info1 = new JLabel("✅ Webcam: Working");
        JLabel info2 = new JLabel("🎯 Auto-detection");
        JLabel info3 = new JLabel("🔊 Sound alerts");
        JLabel info4 = new JLabel("📊 17+ formats");
        
        info1.setAlignmentX(Component.CENTER_ALIGNMENT);
        info2.setAlignmentX(Component.CENTER_ALIGNMENT);
        info3.setAlignmentX(Component.CENTER_ALIGNMENT);
        info4.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        info1.setFont(new Font("Arial", Font.PLAIN, 10));
        info2.setFont(new Font("Arial", Font.PLAIN, 10));
        info3.setFont(new Font("Arial", Font.PLAIN, 10));
        info4.setFont(new Font("Arial", Font.PLAIN, 10));
        
        infoPanel.add(info1);
        infoPanel.add(info2);
        infoPanel.add(info3);
        infoPanel.add(info4);
        
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(startButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(stopButton);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(statusLabel);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(infoPanel);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(clearButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(helpButton);
        controlPanel.add(Box.createVerticalGlue());
        
        // Results panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("🎯 Live Scan Results"));
        resultPanel.setPreferredSize(new java.awt.Dimension(0, 150));
        
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultArea.setText("🎉 Working Webcam Barcode Scanner Ready!\n\n");
        resultArea.append("📹 Click 'Start Live Scanning' to begin\n");
        resultArea.append("🎯 Point camera at any barcode for automatic detection\n");
        resultArea.append("🔊 Listen for beep sound when barcode is detected\n\n");
        resultArea.append("Supported: QR Code, Code 128, EAN-13, UPC-A, and more!\n");
        
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
        startButton.addActionListener(e -> startLiveScanning());
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
            webcam = Webcam.getDefault();
            
            if (webcam != null) {
                updateStatus("✅ Camera detected: " + webcam.getName(), Color.GREEN);
                resultArea.append("✅ Camera found: " + webcam.getName() + "\n");
            } else {
                updateStatus("❌ No camera found", Color.RED);
                resultArea.append("❌ No camera detected\n");
                startButton.setText("❌ No Camera");
                startButton.setEnabled(false);
            }
        } catch (Exception e) {
            updateStatus("❌ Camera error: " + e.getMessage(), Color.RED);
            resultArea.append("❌ Camera error: " + e.getMessage() + "\n");
            startButton.setText("❌ Camera Error");
            startButton.setEnabled(false);
        }
    }
    
    private void startLiveScanning() {
        try {
            updateStatus("🚀 Starting camera...", Color.BLUE);
            
            if (webcam == null) {
                throw new RuntimeException("No camera available");
            }
            
            // Set camera resolution
            java.awt.Dimension[] sizes = webcam.getViewSizes();
            java.awt.Dimension selectedSize = new java.awt.Dimension(640, 480);

            // Try to find best resolution
            for (java.awt.Dimension size : sizes) {
                if (size.width >= 320 && size.height >= 240) {
                    selectedSize = size;
                    if (size.width == 640 && size.height == 480) {
                        break; // Prefer 640x480 if available
                    }
                }
            }
            
            webcam.setViewSize(selectedSize);
            
            // Open camera
            if (!webcam.open()) {
                throw new RuntimeException("Failed to open camera");
            }
            
            isScanning.set(true);
            cameraTimer.start();
            
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            
            updateStatus("🎯 Live scanning active - Point at barcode!", Color.GREEN);
            cameraLabel.setText("📹 Camera starting...");
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            resultArea.append("[" + timestamp + "] 🚀 Live scanning started\n");
            
        } catch (Exception ex) {
            updateStatus("❌ Error: " + ex.getMessage(), Color.RED);
            JOptionPane.showMessageDialog(this, 
                "Failed to start camera: " + ex.getMessage() + 
                "\n\nTroubleshooting:\n" +
                "• Close other camera apps (Skype, Teams, etc.)\n" +
                "• Check Windows camera permissions\n" +
                "• Try restarting the application", 
                "Camera Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void stopScanning() {
        isScanning.set(false);
        
        if (cameraTimer != null) {
            cameraTimer.stop();
        }
        
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
        
        cameraLabel.setIcon(null);
        cameraLabel.setText("📹 Camera stopped - Click Start to resume");
        
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        
        updateStatus("⏹ Scanning stopped", Color.ORANGE);
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        resultArea.append("[" + timestamp + "] ⏹ Live scanning stopped\n");
    }
    
    private void captureAndProcessFrame() {
        try {
            if (webcam != null && webcam.isOpen()) {
                BufferedImage image = webcam.getImage();
                
                if (image != null) {
                    // Update camera display
                    ImageIcon icon = new ImageIcon(image.getScaledInstance(640, 480, Image.SCALE_FAST));
                    cameraLabel.setIcon(icon);
                    cameraLabel.setText("");
                    
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
            
            // Avoid duplicate detections
            long currentTime = System.currentTimeMillis();
            if (!barcodeText.equals(lastDetectedBarcode) || 
                (currentTime - lastDetectionTime) > DETECTION_COOLDOWN_MS) {
                
                lastDetectedBarcode = barcodeText;
                lastDetectionTime = currentTime;
                
                // Add to results
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                String resultText = String.format("[%s] 🎯 %s: %s\n", timestamp, format, barcodeText);
                resultArea.append(resultText);
                resultArea.setCaretPosition(resultArea.getDocument().getLength());
                
                updateStatus("🎉 DETECTED: " + format, Color.GREEN);
                
                // Visual feedback - green border flash
                cameraLabel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 8));
                Timer flashTimer = new Timer(1500, e -> {
                    cameraLabel.setBorder(BorderFactory.createLoweredBevelBorder());
                });
                flashTimer.setRepeats(false);
                flashTimer.start();
                
                // Audio feedback
                Toolkit.getDefaultToolkit().beep();
                
                // Show popup notification
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "🎯 Barcode Detected!\n\n" +
                        "Format: " + format + "\n" +
                        "Content: " + barcodeText + "\n\n" +
                        "Scanning continues...", 
                        "Barcode Found!", 
                        JOptionPane.INFORMATION_MESSAGE);
                });
            }
            
        } catch (NotFoundException e) {
            // No barcode found - this is normal
        } catch (Exception e) {
            // Other errors - log but don't spam the user
            System.err.println("Barcode detection error: " + e.getMessage());
        }
    }
    
    private void showHelp() {
        String helpText = "🎯 Working Webcam Barcode Scanner Help\n\n" +
                         "🚀 HOW TO USE:\n" +
                         "1. Click 'Start Live Scanning' to activate camera\n" +
                         "2. Point camera at any barcode or QR code\n" +
                         "3. Detection happens automatically!\n" +
                         "4. Listen for beep sound when detected\n" +
                         "5. Results appear with popup notification\n" +
                         "6. Click 'Stop Scanning' when finished\n\n" +
                         "✅ SUPPORTED FORMATS:\n" +
                         "QR Code, Code 128, Code 39, EAN-13, UPC-A,\n" +
                         "Data Matrix, PDF417, and 10+ more!\n\n" +
                         "💡 TIPS FOR BEST RESULTS:\n" +
                         "• Ensure good lighting\n" +
                         "• Hold barcode 6-12 inches from camera\n" +
                         "• Keep barcode flat and steady\n" +
                         "• Use high-contrast barcodes\n\n" +
                         "🔧 TROUBLESHOOTING:\n" +
                         "• Close other camera apps first\n" +
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
        stopScanning();
    }
    
    public static void main(String[] args) {
        System.out.println("=== Working Webcam Barcode Scanner Starting ===");
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                WorkingWebcamScanner scanner = new WorkingWebcamScanner();
                scanner.setVisible(true);
                System.out.println("=== Working Webcam Scanner GUI is now visible ===");
            } catch (Exception e) {
                System.err.println("Failed to create GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
