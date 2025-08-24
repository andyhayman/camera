package com.example.barcodescanner;

import com.github.sarxos.webcam.Webcam;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Diagnostic tool to test and improve barcode detection
 */
public class DetectionDiagnostic extends JFrame {
    
    private Webcam webcam;
    private JLabel cameraLabel;
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private JButton captureButton;
    private Timer cameraTimer;
    private MultiFormatReader barcodeReader;
    private boolean isRunning = false;
    private int frameCount = 0;
    private int detectionAttempts = 0;
    
    public DetectionDiagnostic() {
        setTitle("🔍 Barcode Detection Diagnostic Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        initializeComponents();
        setupUI();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Initialize enhanced barcode reader
        barcodeReader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        barcodeReader.setHints(hints);
        
        // Camera timer
        cameraTimer = new Timer(200, new ActionListener() { // 5 FPS for detailed analysis
            @Override
            public void actionPerformed(ActionEvent e) {
                if (webcam != null && webcam.isOpen() && isRunning) {
                    captureAndAnalyze();
                }
            }
        });
        
        log("Diagnostic tool initialized");
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Camera panel
        JPanel cameraPanel = new JPanel(new BorderLayout());
        cameraPanel.setBorder(BorderFactory.createTitledBorder("📹 Live Camera Feed"));
        
        cameraLabel = new JLabel("Camera not started", SwingConstants.CENTER);
        cameraLabel.setPreferredSize(new java.awt.Dimension(640, 480));
        cameraLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        cameraLabel.setBackground(Color.BLACK);
        cameraLabel.setOpaque(true);
        cameraLabel.setForeground(Color.WHITE);
        
        cameraPanel.add(cameraLabel, BorderLayout.CENTER);
        
        // Control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("🎮 Controls"));
        controlPanel.setPreferredSize(new java.awt.Dimension(200, 0));
        
        startButton = new JButton("▶️ Start Camera");
        stopButton = new JButton("⏹️ Stop Camera");
        captureButton = new JButton("📸 Test Capture");
        JButton clearButton = new JButton("🗑️ Clear Log");
        
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        captureButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        stopButton.setEnabled(false);
        captureButton.setEnabled(false);
        
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(startButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(stopButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(captureButton);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(clearButton);
        controlPanel.add(Box.createVerticalGlue());
        
        // Log panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("🔍 Detection Analysis Log"));
        logPanel.setPreferredSize(new java.awt.Dimension(0, 250));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        logPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Layout
        add(cameraPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        add(logPanel, BorderLayout.SOUTH);
        
        // Event handlers
        clearButton.addActionListener(e -> {
            logArea.setText("");
            frameCount = 0;
            detectionAttempts = 0;
            log("Log cleared - Ready for new analysis");
        });
    }
    
    private void setupEventHandlers() {
        startButton.addActionListener(e -> startCamera());
        stopButton.addActionListener(e -> stopCamera());
        captureButton.addActionListener(e -> testSingleCapture());
    }
    
    private void startCamera() {
        try {
            log("🚀 Starting camera diagnostic...");
            
            webcam = Webcam.getDefault();
            if (webcam == null) {
                log("❌ No webcam found");
                return;
            }
            
            log("✅ Webcam found: " + webcam.getName());
            
            // Set highest available resolution
            java.awt.Dimension[] sizes = webcam.getViewSizes();
            java.awt.Dimension bestSize = sizes[0];
            
            log("📐 Available resolutions:");
            for (java.awt.Dimension size : sizes) {
                log("   - " + size.width + "x" + size.height);
                if (size.width * size.height > bestSize.width * bestSize.height) {
                    bestSize = size;
                }
            }
            
            webcam.setViewSize(bestSize);
            log("🎯 Selected resolution: " + bestSize.width + "x" + bestSize.height);
            
            if (!webcam.open()) {
                log("❌ Failed to open webcam");
                return;
            }
            
            log("✅ Webcam opened successfully");
            
            isRunning = true;
            cameraTimer.start();
            
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            captureButton.setEnabled(true);
            
            log("🔍 Starting detection analysis...");
            log("📊 Frame analysis will appear below:");
            
        } catch (Exception ex) {
            log("❌ Error starting camera: " + ex.getMessage());
        }
    }
    
    private void stopCamera() {
        isRunning = false;
        
        if (cameraTimer != null) {
            cameraTimer.stop();
        }
        
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
        
        cameraLabel.setIcon(null);
        cameraLabel.setText("Camera stopped");
        
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        captureButton.setEnabled(false);
        
        log("⏹️ Camera stopped");
        log("📊 Analysis Summary:");
        log("   Total frames processed: " + frameCount);
        log("   Detection attempts: " + detectionAttempts);
        if (detectionAttempts > 0) {
            log("   Success rate: " + (detectionAttempts > 0 ? "Some attempts made" : "No detections"));
        }
    }
    
    private void captureAndAnalyze() {
        try {
            BufferedImage image = webcam.getImage();
            if (image != null) {
                frameCount++;
                
                // Update display
                ImageIcon icon = new ImageIcon(image.getScaledInstance(640, 480, Image.SCALE_FAST));
                cameraLabel.setIcon(icon);
                cameraLabel.setText("");
                
                // Analyze frame every 10 frames (every 2 seconds at 5 FPS)
                if (frameCount % 10 == 0) {
                    analyzeFrame(image, frameCount);
                }
            }
        } catch (Exception e) {
            log("❌ Frame capture error: " + e.getMessage());
        }
    }
    
    private void testSingleCapture() {
        try {
            if (webcam != null && webcam.isOpen()) {
                BufferedImage image = webcam.getImage();
                if (image != null) {
                    log("📸 Manual capture test:");
                    analyzeFrame(image, -1);
                }
            }
        } catch (Exception e) {
            log("❌ Manual capture error: " + e.getMessage());
        }
    }
    
    private void analyzeFrame(BufferedImage image, int frameNum) {
        detectionAttempts++;
        
        String prefix = frameNum > 0 ? "[Frame " + frameNum + "] " : "[Manual] ";
        log(prefix + "🔍 Analyzing " + image.getWidth() + "x" + image.getHeight() + " image...");
        
        try {
            // Test 1: Basic detection
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
            try {
                Result result = barcodeReader.decode(bitmap);
                log(prefix + "🎯 SUCCESS! Detected " + result.getBarcodeFormat() + ": " + result.getText());
                
                // Flash green border
                cameraLabel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 5));
                Timer flashTimer = new Timer(1000, e -> {
                    cameraLabel.setBorder(BorderFactory.createLoweredBevelBorder());
                });
                flashTimer.setRepeats(false);
                flashTimer.start();
                
                Toolkit.getDefaultToolkit().beep();
                return;
                
            } catch (NotFoundException e) {
                log(prefix + "❌ No barcode found with standard detection");
            }
            
            // Test 2: Try with different preprocessing
            log(prefix + "🔄 Trying enhanced detection methods...");
            
            // Convert to grayscale and enhance contrast
            BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g2d = grayImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            
            LuminanceSource graySource = new BufferedImageLuminanceSource(grayImage);
            BinaryBitmap grayBitmap = new BinaryBitmap(new HybridBinarizer(graySource));
            
            try {
                Result result = barcodeReader.decode(grayBitmap);
                log(prefix + "🎯 SUCCESS with grayscale! " + result.getBarcodeFormat() + ": " + result.getText());
                return;
            } catch (NotFoundException e) {
                log(prefix + "❌ No barcode found with grayscale");
            }
            
            // Test 3: Image quality analysis
            analyzeImageQuality(image, prefix);
            
        } catch (Exception e) {
            log(prefix + "❌ Analysis error: " + e.getMessage());
        }
    }
    
    private void analyzeImageQuality(BufferedImage image, String prefix) {
        // Check image brightness
        long totalBrightness = 0;
        int pixelCount = image.getWidth() * image.getHeight();
        
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color color = new Color(image.getRGB(x, y));
                totalBrightness += (color.getRed() + color.getGreen() + color.getBlue()) / 3;
            }
        }
        
        int avgBrightness = (int) (totalBrightness / pixelCount);
        
        log(prefix + "📊 Image quality analysis:");
        log(prefix + "   Resolution: " + image.getWidth() + "x" + image.getHeight());
        log(prefix + "   Average brightness: " + avgBrightness + "/255");
        
        if (avgBrightness < 50) {
            log(prefix + "⚠️  Image too dark - try better lighting");
        } else if (avgBrightness > 200) {
            log(prefix + "⚠️  Image too bright - reduce lighting");
        } else {
            log(prefix + "✅ Brightness looks good");
        }
        
        log(prefix + "💡 Tips for better detection:");
        log(prefix + "   • Ensure good lighting (not too bright/dark)");
        log(prefix + "   • Hold barcode steady and flat");
        log(prefix + "   • Try different distances (6-12 inches)");
        log(prefix + "   • Use high-contrast barcodes (black on white)");
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
            );
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Use default look and feel
            }
            
            new DetectionDiagnostic().setVisible(true);
        });
    }
}
