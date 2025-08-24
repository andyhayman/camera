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
 * Swing-based barcode scanner application (alternative to JavaFX)
 */
public class SwingBarcodeScanner extends JFrame {
    
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
    
    public SwingBarcodeScanner() {
        initializeComponents();
        setupBarcodeReader();
        setupUI();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setTitle("Barcode Scanner - Swing Version");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Initialize barcode reader
        barcodeReader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.values());
        barcodeReader.setHints(hints);
    }
    
    private void setupBarcodeReader() {
        // Camera timer for capturing frames
        cameraTimer = new Timer(33, new ActionListener() { // ~30 FPS
            @Override
            public void actionPerformed(ActionEvent e) {
                if (webcam != null && webcam.isOpen() && isScanning.get()) {
                    try {
                        BufferedImage image = webcam.getImage();
                        if (image != null) {
                            // Update camera display
                            ImageIcon icon = new ImageIcon(image.getScaledInstance(640, 480, Image.SCALE_FAST));
                            cameraLabel.setIcon(icon);
                            
                            // Try to detect barcode
                            detectBarcode(image);
                        }
                    } catch (Exception ex) {
                        updateStatus("Error capturing frame: " + ex.getMessage(), Color.RED);
                    }
                }
            }
        });
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

        cameraPanel.add(cameraLabel, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
        controlPanel.setPreferredSize(new java.awt.Dimension(200, 0));
        
        startButton = new JButton("Start Scanning");
        stopButton = new JButton("Stop Scanning");
        JButton clearButton = new JButton("Clear Results");
        
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        stopButton.setEnabled(false);
        
        statusLabel = new JLabel("Ready to scan");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(Color.BLUE);
        
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(startButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(stopButton);
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
        resultArea.setText("Scanned barcodes will appear here...\n");
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Layout
        add(cameraPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        add(resultPanel, BorderLayout.SOUTH);
        
        // Event handlers
        clearButton.addActionListener(e -> {
            resultArea.setText("Scanned barcodes will appear here...\n");
            updateStatus("Results cleared", Color.BLUE);
        });
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
    }
    
    private void startScanning() {
        try {
            updateStatus("Starting camera...", Color.BLUE);
            
            // Get webcam
            webcam = Webcam.getDefault();
            if (webcam == null) {
                throw new RuntimeException("No webcam found");
            }
            
            // Set resolution
            java.awt.Dimension[] sizes = webcam.getViewSizes();
            java.awt.Dimension selectedSize = new java.awt.Dimension(640, 480);
            for (java.awt.Dimension size : sizes) {
                if (size.width == 640 && size.height == 480) {
                    selectedSize = size;
                    break;
                }
            }
            webcam.setViewSize(selectedSize);
            
            // Open webcam
            if (!webcam.open()) {
                throw new RuntimeException("Failed to open webcam");
            }
            
            isScanning.set(true);
            cameraTimer.start();
            
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            updateStatus("Scanning for barcodes...", Color.GREEN);
            
        } catch (Exception ex) {
            updateStatus("Error: " + ex.getMessage(), Color.RED);
            JOptionPane.showMessageDialog(this, 
                "Failed to start camera: " + ex.getMessage(), 
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
        cameraLabel.setText("Camera stopped");
        
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        updateStatus("Scanning stopped", Color.ORANGE);
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
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
        }
        
        SwingUtilities.invokeLater(() -> {
            new SwingBarcodeScanner().setVisible(true);
        });
    }
}
