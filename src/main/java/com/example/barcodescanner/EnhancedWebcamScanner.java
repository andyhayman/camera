package com.example.barcodescanner;

import com.github.sarxos.webcam.Webcam;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.common.GlobalHistogramBinarizer;

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
 * Enhanced webcam barcode scanner with improved detection algorithms
 */
public class EnhancedWebcamScanner extends JFrame {
    
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
    private static final long DETECTION_COOLDOWN_MS = 1000; // Reduced cooldown
    private int frameCount = 0;
    private int detectionAttempts = 0;
    private int successfulDetections = 0;
    
    public EnhancedWebcamScanner() {
        System.out.println("Initializing Enhanced Webcam Barcode Scanner...");
        
        initializeComponents();
        setupEnhancedBarcodeReader();
        setupUI();
        setupEventHandlers();
        checkWebcamAvailability();
        
        System.out.println("Enhanced GUI initialization complete!");
    }
    
    private void initializeComponents() {
        setTitle("🎯 Enhanced Webcam Barcode Scanner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        System.out.println("Components initialized");
    }
    
    private void setupEnhancedBarcodeReader() {
        // Initialize enhanced barcode reader with optimized settings
        barcodeReader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new HashMap<>();
        
        // Enhanced detection hints
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        
        // Enable all barcode formats for maximum compatibility
        hints.put(DecodeHintType.POSSIBLE_FORMATS, java.util.Arrays.asList(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.CODE_128,
            BarcodeFormat.CODE_39,
            BarcodeFormat.CODE_93,
            BarcodeFormat.EAN_13,
            BarcodeFormat.EAN_8,
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.CODABAR,
            BarcodeFormat.ITF,
            BarcodeFormat.RSS_14,
            BarcodeFormat.RSS_EXPANDED,
            BarcodeFormat.DATA_MATRIX,
            BarcodeFormat.PDF_417,
            BarcodeFormat.AZTEC,
            BarcodeFormat.MAXICODE
        ));
        
        barcodeReader.setHints(hints);
        
        // Camera timer with faster frame rate for better detection
        cameraTimer = new Timer(50, new ActionListener() { // 20 FPS
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
        
        System.out.println("Enhanced barcode reader setup complete");
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Camera panel
        JPanel cameraPanel = new JPanel(new BorderLayout());
        cameraPanel.setBorder(BorderFactory.createTitledBorder("📹 Enhanced Live Camera Feed"));
        
        cameraLabel = new JLabel("Enhanced scanner ready - Click Start", SwingConstants.CENTER);
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
        controlPanel.setBorder(BorderFactory.createTitledBorder("🚀 Enhanced Controls"));
        controlPanel.setPreferredSize(new java.awt.Dimension(220, 0));
        
        startButton = new JButton("🎯 Start Enhanced Scanning");
        stopButton = new JButton("⏹ Stop Scanning");
        JButton clearButton = new JButton("🗑 Clear Results");
        JButton diagnosticButton = new JButton("🔍 Run Diagnostic");
        JButton helpButton = new JButton("❓ Help");
        
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        diagnosticButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        helpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        stopButton.setEnabled(false);
        
        statusLabel = new JLabel("Enhanced scanner ready");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(Color.GREEN);
        
        // Stats panel
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createTitledBorder("📊 Detection Stats"));
        
        JLabel stats1 = new JLabel("⚡ 20 FPS processing");
        JLabel stats2 = new JLabel("🎯 Multi-algorithm detection");
        JLabel stats3 = new JLabel("📊 16+ barcode formats");
        JLabel stats4 = new JLabel("🔧 Auto image enhancement");
        
        stats1.setAlignmentX(Component.CENTER_ALIGNMENT);
        stats2.setAlignmentX(Component.CENTER_ALIGNMENT);
        stats3.setAlignmentX(Component.CENTER_ALIGNMENT);
        stats4.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        stats1.setFont(new Font("Arial", Font.PLAIN, 10));
        stats2.setFont(new Font("Arial", Font.PLAIN, 10));
        stats3.setFont(new Font("Arial", Font.PLAIN, 10));
        stats4.setFont(new Font("Arial", Font.PLAIN, 10));
        
        statsPanel.add(stats1);
        statsPanel.add(stats2);
        statsPanel.add(stats3);
        statsPanel.add(stats4);
        
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(startButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(stopButton);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(statusLabel);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(statsPanel);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(clearButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(diagnosticButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(helpButton);
        controlPanel.add(Box.createVerticalGlue());
        
        // Results panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("🎯 Enhanced Detection Results"));
        resultPanel.setPreferredSize(new java.awt.Dimension(0, 150));
        
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultArea.setText("🚀 Enhanced Webcam Barcode Scanner Ready!\n\n");
        resultArea.append("🎯 ENHANCED FEATURES:\n");
        resultArea.append("• 20 FPS high-speed processing\n");
        resultArea.append("• Multi-algorithm detection (3 methods per frame)\n");
        resultArea.append("• Auto image enhancement and preprocessing\n");
        resultArea.append("• 16+ barcode format support\n");
        resultArea.append("• Improved low-light detection\n\n");
        resultArea.append("Click 'Start Enhanced Scanning' to begin!\n");
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Layout
        add(cameraPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        add(resultPanel, BorderLayout.SOUTH);
        
        // Event handlers for buttons
        clearButton.addActionListener(e -> {
            resultArea.setText("Results cleared.\nReady for enhanced scanning...\n");
            frameCount = 0;
            detectionAttempts = 0;
            successfulDetections = 0;
            updateStatus("Results cleared", Color.BLUE);
        });
        
        diagnosticButton.addActionListener(e -> runDiagnostic());
        helpButton.addActionListener(e -> showEnhancedHelp());
        
        System.out.println("Enhanced UI setup complete");
    }
    
    private void setupEventHandlers() {
        startButton.addActionListener(e -> startEnhancedScanning());
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
                updateStatus("✅ Enhanced scanner ready: " + webcam.getName(), Color.GREEN);
                resultArea.append("✅ Camera detected: " + webcam.getName() + "\n");
                resultArea.append("🎯 Ready for enhanced barcode detection!\n");
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
    
    private void startEnhancedScanning() {
        try {
            updateStatus("🚀 Starting enhanced scanning...", Color.BLUE);
            
            if (webcam == null) {
                throw new RuntimeException("No camera available");
            }
            
            // Set highest available resolution for better detection
            java.awt.Dimension[] sizes = webcam.getViewSizes();
            java.awt.Dimension bestSize = sizes[0];
            
            for (java.awt.Dimension size : sizes) {
                if (size.width * size.height > bestSize.width * bestSize.height) {
                    bestSize = size;
                }
            }
            
            webcam.setViewSize(bestSize);
            
            // Open camera
            if (!webcam.open()) {
                throw new RuntimeException("Failed to open camera");
            }
            
            isScanning.set(true);
            cameraTimer.start();
            
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            
            updateStatus("🎯 Enhanced scanning active - 20 FPS processing!", Color.GREEN);
            cameraLabel.setText("📹 Enhanced camera starting...");
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            resultArea.append("[" + timestamp + "] 🚀 Enhanced scanning started\n");
            resultArea.append("Resolution: " + bestSize.width + "x" + bestSize.height + "\n");
            resultArea.append("Processing: 20 FPS with 3 detection algorithms\n\n");
            
        } catch (Exception ex) {
            updateStatus("❌ Error: " + ex.getMessage(), Color.RED);
            JOptionPane.showMessageDialog(this, 
                "Failed to start enhanced scanning: " + ex.getMessage() + 
                "\n\nTroubleshooting:\n" +
                "• Close other camera apps\n" +
                "• Check camera permissions\n" +
                "• Try running diagnostic", 
                "Enhanced Scanner Error", 
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
        cameraLabel.setText("📹 Enhanced scanner stopped");
        
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        
        updateStatus("⏹ Enhanced scanning stopped", Color.ORANGE);
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        resultArea.append("[" + timestamp + "] ⏹ Enhanced scanning stopped\n");
        
        if (frameCount > 0) {
            resultArea.append("📊 Session stats: " + frameCount + " frames, " + 
                             detectionAttempts + " attempts, " + successfulDetections + " detections\n");
        }
    }
    
    private void captureAndProcessFrame() {
        try {
            if (webcam != null && webcam.isOpen()) {
                BufferedImage image = webcam.getImage();
                
                if (image != null) {
                    frameCount++;
                    
                    // Update camera display
                    ImageIcon icon = new ImageIcon(image.getScaledInstance(640, 480, Image.SCALE_FAST));
                    cameraLabel.setIcon(icon);
                    cameraLabel.setText("");
                    
                    // Try enhanced barcode detection
                    enhancedBarcodeDetection(image);
                }
            }
        } catch (Exception e) {
            updateStatus("Frame capture error", Color.RED);
        }
    }
    
    private void enhancedBarcodeDetection(BufferedImage image) {
        detectionAttempts++;
        
        try {
            // Method 1: Standard detection with HybridBinarizer
            Result result = tryDetection(image, new HybridBinarizer(new BufferedImageLuminanceSource(image)));
            if (result != null) {
                handleDetection(result, "Standard");
                return;
            }
            
            // Method 2: Global histogram binarizer
            result = tryDetection(image, new GlobalHistogramBinarizer(new BufferedImageLuminanceSource(image)));
            if (result != null) {
                handleDetection(result, "Global");
                return;
            }
            
            // Method 3: Enhanced preprocessing
            BufferedImage enhancedImage = enhanceImage(image);
            result = tryDetection(enhancedImage, new HybridBinarizer(new BufferedImageLuminanceSource(enhancedImage)));
            if (result != null) {
                handleDetection(result, "Enhanced");
                return;
            }
            
        } catch (Exception e) {
            // Silent failure for individual frames
        }
    }
    
    private Result tryDetection(BufferedImage image, Binarizer binarizer) {
        try {
            BinaryBitmap bitmap = new BinaryBitmap(binarizer);
            return barcodeReader.decode(bitmap);
        } catch (NotFoundException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private BufferedImage enhanceImage(BufferedImage original) {
        // Create enhanced version with better contrast
        BufferedImage enhanced = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = enhanced.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();
        
        // Simple contrast enhancement
        for (int x = 0; x < enhanced.getWidth(); x++) {
            for (int y = 0; y < enhanced.getHeight(); y++) {
                Color color = new Color(enhanced.getRGB(x, y));
                int gray = color.getRed(); // Already grayscale
                
                // Enhance contrast
                gray = Math.min(255, Math.max(0, (int) ((gray - 128) * 1.5 + 128)));
                
                Color newColor = new Color(gray, gray, gray);
                enhanced.setRGB(x, y, newColor.getRGB());
            }
        }
        
        return enhanced;
    }
    
    private void handleDetection(Result result, String method) {
        String barcodeText = result.getText();
        String format = result.getBarcodeFormat().toString();
        
        // Avoid duplicate detections
        long currentTime = System.currentTimeMillis();
        if (!barcodeText.equals(lastDetectedBarcode) || 
            (currentTime - lastDetectionTime) > DETECTION_COOLDOWN_MS) {
            
            lastDetectedBarcode = barcodeText;
            lastDetectionTime = currentTime;
            successfulDetections++;
            
            // Add to results
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String resultText = String.format("[%s] 🎯 %s (%s): %s\n", timestamp, format, method, barcodeText);
            resultArea.append(resultText);
            resultArea.setCaretPosition(resultArea.getDocument().getLength());
            
            updateStatus("🎉 DETECTED with " + method + ": " + format, Color.GREEN);
            
            // Enhanced visual feedback
            cameraLabel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 10));
            Timer flashTimer = new Timer(2000, e -> {
                cameraLabel.setBorder(BorderFactory.createLoweredBevelBorder());
            });
            flashTimer.setRepeats(false);
            flashTimer.start();
            
            // Audio feedback
            Toolkit.getDefaultToolkit().beep();
        }
    }
    
    private void runDiagnostic() {
        String diagnosticInfo = "🔍 DIAGNOSTIC INFORMATION:\n\n" +
                               "📊 Current Session Stats:\n" +
                               "• Frames processed: " + frameCount + "\n" +
                               "• Detection attempts: " + detectionAttempts + "\n" +
                               "• Successful detections: " + successfulDetections + "\n\n" +
                               "🎯 Detection Rate: " +
                               (detectionAttempts > 0 ? (successfulDetections * 100 / detectionAttempts) + "%" : "N/A") + "\n\n" +
                               "💡 TIPS TO IMPROVE DETECTION:\n" +
                               "• Ensure good lighting (not too bright/dark)\n" +
                               "• Hold barcode 6-12 inches from camera\n" +
                               "• Keep barcode flat and steady\n" +
                               "• Use high-contrast barcodes (black on white)\n" +
                               "• Clean camera lens\n" +
                               "• Try different angles\n" +
                               "• Use larger barcodes when possible";

        JOptionPane.showMessageDialog(this, diagnosticInfo, "Detection Diagnostic", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showEnhancedHelp() {
        String helpText = "🎯 Enhanced Webcam Barcode Scanner Help\n\n" +
                         "🚀 ENHANCED FEATURES:\n" +
                         "• 20 FPS high-speed processing\n" +
                         "• 3 detection algorithms per frame\n" +
                         "• Auto image enhancement\n" +
                         "• 16+ barcode format support\n" +
                         "• Improved low-light detection\n\n" +
                         "🎯 HOW TO USE:\n" +
                         "1. Click 'Start Enhanced Scanning'\n" +
                         "2. Point camera at barcode (6-12 inches away)\n" +
                         "3. Keep barcode steady and flat\n" +
                         "4. Listen for beep when detected\n" +
                         "5. See results with detection method\n\n" +
                         "💡 TIPS FOR BEST RESULTS:\n" +
                         "• Use good lighting (not too bright/dark)\n" +
                         "• Hold barcode steady\n" +
                         "• Try different angles if needed\n" +
                         "• Use high-contrast barcodes\n" +
                         "• Clean camera lens\n\n" +
                         "🔧 TROUBLESHOOTING:\n" +
                         "• Click 'Run Diagnostic' for detailed analysis\n" +
                         "• Close other camera applications\n" +
                         "• Check lighting conditions\n" +
                         "• Try different barcode distances";
        
        JOptionPane.showMessageDialog(this, helpText, "Enhanced Scanner Help", JOptionPane.INFORMATION_MESSAGE);
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
        System.out.println("=== Enhanced Webcam Barcode Scanner Starting ===");
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                EnhancedWebcamScanner scanner = new EnhancedWebcamScanner();
                scanner.setVisible(true);
                System.out.println("=== Enhanced Scanner GUI is now visible ===");
            } catch (Exception e) {
                System.err.println("Failed to create enhanced GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
