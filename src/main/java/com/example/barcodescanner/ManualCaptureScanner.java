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
 * Manual capture barcode scanner with improved detection algorithms
 */
public class ManualCaptureScanner extends JFrame {
    
    private Webcam webcam;
    private JLabel cameraLabel;
    private JTextArea resultArea;
    private JButton startButton;
    private JButton stopButton;
    private JButton captureButton;
    private JLabel statusLabel;
    private Timer cameraTimer;
    private MultiFormatReader barcodeReader;
    private BufferedImage lastCapturedImage;
    
    private final AtomicBoolean isScanning = new AtomicBoolean(false);
    
    public ManualCaptureScanner() {
        System.out.println("Initializing Manual Capture Barcode Scanner...");
        
        initializeComponents();
        setupEnhancedBarcodeReader();
        setupUI();
        setupEventHandlers();
        checkWebcamAvailability();
        
        System.out.println("Manual Capture Scanner initialization complete!");
    }
    
    private void initializeComponents() {
        setTitle("📸 Manual Capture Barcode Scanner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        System.out.println("Components initialized");
    }
    
    private void setupEnhancedBarcodeReader() {
        barcodeReader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new HashMap<>();
        
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
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
            BarcodeFormat.DATA_MATRIX,
            BarcodeFormat.PDF_417,
            BarcodeFormat.AZTEC
        ));
        
        barcodeReader.setHints(hints);
        
        // Camera timer for preview only
        cameraTimer = new Timer(33, e -> updatePreview()); // 30 FPS preview
        
        System.out.println("Enhanced barcode reader setup complete");
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Camera panel
        JPanel cameraPanel = new JPanel(new BorderLayout());
        cameraPanel.setBorder(BorderFactory.createTitledBorder("📹 Camera Preview"));
        
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
        controlPanel.setBorder(BorderFactory.createTitledBorder("🎮 Controls"));
        
        startButton = new JButton("🚀 Start Camera");
        stopButton = new JButton("⏹ Stop Camera");
        captureButton = new JButton("📸 Take Picture");
        JButton clearButton = new JButton("🗑 Clear Results");
        
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
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(clearButton);
        
        // Status and info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("ℹ️ Status"));
        
        statusLabel = new JLabel("Ready to start");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        infoPanel.add(statusLabel);
        
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(infoPanel);
        
        // Results panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("🎯 Scan Results"));
        
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        
        resultPanel.add(scrollPane);
        resultPanel.setPreferredSize(new java.awt.Dimension(0, 150));
        
        // Main layout
        add(cameraPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        add(resultPanel, BorderLayout.SOUTH);
        
        System.out.println("UI setup complete");
    }
    
    private void setupEventHandlers() {
        startButton.addActionListener(e -> startCamera());
        stopButton.addActionListener(e -> stopCamera());
        captureButton.addActionListener(e -> captureAndScan());
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
        
        System.out.println("Event handlers setup complete");
    }
    
    private void updatePreview() {
        if (webcam != null && webcam.isOpen()) {
            try {
                BufferedImage image = webcam.getImage();
                if (image != null) {
                    ImageIcon icon = new ImageIcon(image.getScaledInstance(640, 480, Image.SCALE_FAST));
                    cameraLabel.setIcon(icon);
                    cameraLabel.setText("");
                }
            } catch (Exception e) {
                updateStatus("Preview error: " + e.getMessage(), Color.RED);
            }
        }
    }
    
    private void captureAndScan() {
        if (webcam != null && webcam.isOpen()) {
            try {
                // Capture image
                lastCapturedImage = webcam.getImage();
                if (lastCapturedImage != null) {
                    // Visual feedback
                    cameraLabel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
                    Timer flashTimer = new Timer(500, e -> {
                        cameraLabel.setBorder(BorderFactory.createLoweredBevelBorder());
                    });
                    flashTimer.setRepeats(false);
                    flashTimer.start();
                    
                    // Play camera sound
                    Toolkit.getDefaultToolkit().beep();
                    
                    // Try to detect barcode
                    detectBarcodeInImage(lastCapturedImage);
                }
            } catch (Exception e) {
                updateStatus("Capture error: " + e.getMessage(), Color.RED);
            }
        }
    }
    
    private void detectBarcodeInImage(BufferedImage image) {
        try {
            updateStatus("Analyzing image...", Color.BLUE);
            
            // Try with HybridBinarizer first
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            Result result = null;
            
            try {
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                result = barcodeReader.decode(bitmap);
            } catch (NotFoundException e) {
                // If HybridBinarizer fails, try GlobalHistogramBinarizer
                try {
                    BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
                    result = barcodeReader.decode(bitmap);
                } catch (NotFoundException e2) {
                    // Both failed
                    updateStatus("No barcode found - Try again", Color.ORANGE);
                    return;
                }
            }
            
            if (result != null) {
                String barcodeText = result.getText();
                String format = result.getBarcodeFormat().toString();
                
                // Update results
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                String resultText = String.format("[%s] 🎯 %s: %s\n", timestamp, format, barcodeText);
                resultArea.append(resultText);
                resultArea.setCaretPosition(resultArea.getDocument().getLength());
                
                // Visual feedback
                cameraLabel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
                Timer flashTimer = new Timer(1500, e -> {
                    cameraLabel.setBorder(BorderFactory.createLoweredBevelBorder());
                });
                flashTimer.setRepeats(false);
                flashTimer.start();
                
                // Update status
                updateStatus("Barcode detected! Format: " + format, Color.GREEN);
                
                // Show popup
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "🎯 Barcode Detected!\n\n" +
                        "Format: " + format + "\n" +
                        "Content: " + barcodeText,
                        "Barcode Found!",
                        JOptionPane.INFORMATION_MESSAGE);
                });
            }
            
        } catch (Exception e) {
            updateStatus("Analysis error: " + e.getMessage(), Color.RED);
        }
    }
    
    private void startCamera() {
        try {
            updateStatus("Starting camera...", Color.BLUE);
            
            if (webcam == null) {
                webcam = Webcam.getDefault();
                if (webcam == null) {
                    throw new RuntimeException("No camera available");
                }
            }
            
            // Get all available resolutions
            java.awt.Dimension[] sizes = webcam.getViewSizes();
            
            // Print available resolutions
            resultArea.append("\nAvailable camera resolutions:\n");
            for (java.awt.Dimension size : sizes) {
                resultArea.append(String.format("- %dx%d\n", size.width, size.height));
            }
            
            // Find the highest resolution available
            java.awt.Dimension bestSize = sizes[0];
            for (java.awt.Dimension size : sizes) {
                if (size.width * size.height > bestSize.width * bestSize.height) {
                    bestSize = size;
                }
            }
            
            resultArea.append(String.format("\nSetting resolution to: %dx%d\n", bestSize.width, bestSize.height));
            webcam.setViewSize(bestSize);
            
            if (!webcam.open()) {
                throw new RuntimeException("Failed to open camera");
            }
            
            isScanning.set(true);
            cameraTimer.start();
            
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            captureButton.setEnabled(true);
            
            updateStatus("Camera ready - Click 'Take Picture' to scan", Color.GREEN);
            
        } catch (Exception ex) {
            updateStatus("Camera error: " + ex.getMessage(), Color.RED);
            JOptionPane.showMessageDialog(this,
                "Failed to start camera: " + ex.getMessage(),
                "Camera Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void stopCamera() {
        isScanning.set(false);
        
        if (cameraTimer != null) {
            cameraTimer.stop();
        }
        
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
        
        cameraLabel.setIcon(null);
        cameraLabel.setText("📷 Camera stopped - Click Start to resume");
        
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        captureButton.setEnabled(false);
        
        updateStatus("Camera stopped", Color.ORANGE);
    }
    
    private void cleanup() {
        stopCamera();
    }
    
    private void updateStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
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
                startButton.setEnabled(false);
            }
        } catch (Exception e) {
            updateStatus("❌ Camera error: " + e.getMessage(), Color.RED);
            resultArea.append("❌ Camera error: " + e.getMessage() + "\n");
            startButton.setEnabled(false);
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new ManualCaptureScanner().setVisible(true);
        });
    }
}
