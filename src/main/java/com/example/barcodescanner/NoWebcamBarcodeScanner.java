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
 * Barcode scanner that works with image files instead of webcam
 */
public class NoWebcamBarcodeScanner extends JFrame {
    
    private JLabel imageLabel;
    private JTextArea resultArea;
    private JButton loadImageButton;
    private JButton scanButton;
    private JLabel statusLabel;
    private MultiFormatReader barcodeReader;
    private BufferedImage currentImage;
    
    public NoWebcamBarcodeScanner() {
        System.out.println("Initializing Barcode Scanner (No Webcam Version)...");
        
        initializeComponents();
        setupBarcodeReader();
        setupUI();
        setupEventHandlers();
        
        System.out.println("GUI initialization complete!");
    }
    
    private void initializeComponents() {
        setTitle("Barcode Scanner - Image File Version");
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
        System.out.println("Barcode reader setup complete");
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Image panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createTitledBorder("Barcode Image"));
        
        imageLabel = new JLabel("No image loaded", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new java.awt.Dimension(640, 480));
        imageLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        imageLabel.setBackground(Color.LIGHT_GRAY);
        imageLabel.setOpaque(true);
        imageLabel.setForeground(Color.DARK_GRAY);
        imageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        
        // Control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
        controlPanel.setPreferredSize(new java.awt.Dimension(200, 0));
        
        loadImageButton = new JButton("Load Image");
        scanButton = new JButton("Scan Barcode");
        JButton clearButton = new JButton("Clear Results");
        JButton helpButton = new JButton("Help");
        
        loadImageButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        scanButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        helpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        scanButton.setEnabled(false);
        
        statusLabel = new JLabel("Ready - Load an image to begin");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setForeground(Color.BLUE);
        
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(loadImageButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(scanButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(clearButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(helpButton);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(statusLabel);
        controlPanel.add(Box.createVerticalGlue());
        
        // Results panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Scan Results"));
        resultPanel.setPreferredSize(new java.awt.Dimension(0, 150));
        
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultArea.setText("Barcode Scanner Ready!\n");
        resultArea.append("1. Click 'Load Image' to select a barcode image\n");
        resultArea.append("2. Click 'Scan Barcode' to detect barcodes\n");
        resultArea.append("3. Results will appear here\n\n");
        resultArea.append("Supported formats: QR Code, Code 128, EAN-13, UPC-A, and more!\n");
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Layout
        add(imagePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        add(resultPanel, BorderLayout.SOUTH);
        
        // Event handlers for buttons
        clearButton.addActionListener(e -> {
            resultArea.setText("Results cleared.\nReady for new scans...\n");
            updateStatus("Results cleared", Color.BLUE);
        });
        
        helpButton.addActionListener(e -> showHelp());
        
        System.out.println("UI setup complete");
    }
    
    private void setupEventHandlers() {
        loadImageButton.addActionListener(e -> loadImage());
        scanButton.addActionListener(e -> scanCurrentImage());
        
        System.out.println("Event handlers setup complete");
    }
    
    private void loadImage() {
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
                    // Scale image to fit display
                    ImageIcon icon = new ImageIcon(currentImage.getScaledInstance(640, 480, Image.SCALE_SMOOTH));
                    imageLabel.setIcon(icon);
                    imageLabel.setText("");
                    
                    scanButton.setEnabled(true);
                    updateStatus("Image loaded: " + selectedFile.getName(), Color.GREEN);
                    
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    resultArea.append("[" + timestamp + "] Image loaded: " + selectedFile.getName() + "\n");
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
        if (currentImage == null) {
            updateStatus("No image loaded", Color.RED);
            return;
        }
        
        try {
            updateStatus("Scanning for barcodes...", Color.BLUE);
            
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
            
            updateStatus("Barcode detected: " + format, Color.GREEN);
            
            // Flash the border to indicate detection
            imageLabel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
            Timer flashTimer = new Timer(1000, e -> {
                imageLabel.setBorder(BorderFactory.createLoweredBevelBorder());
            });
            flashTimer.setRepeats(false);
            flashTimer.start();
            
            // Show success dialog
            JOptionPane.showMessageDialog(this, 
                "Barcode detected successfully!\n\n" +
                "Format: " + format + "\n" +
                "Content: " + barcodeText, 
                "Barcode Detected", 
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NotFoundException e) {
            updateStatus("No barcode found in image", Color.ORANGE);
            resultArea.append("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + 
                             "] No barcode detected in image\n");
            
            JOptionPane.showMessageDialog(this, 
                "No barcode found in the image.\n\n" +
                "Tips:\n" +
                "• Ensure the image contains a clear barcode\n" +
                "• Try a higher resolution image\n" +
                "• Make sure the barcode is not rotated\n" +
                "• Check that there's good contrast", 
                "No Barcode Found", 
                JOptionPane.WARNING_MESSAGE);
                
        } catch (Exception e) {
            updateStatus("Scan error: " + e.getMessage(), Color.RED);
            JOptionPane.showMessageDialog(this, 
                "Error scanning barcode: " + e.getMessage(), 
                "Scan Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showHelp() {
        String helpText = "Barcode Scanner Help\n\n" +
                         "How to use:\n" +
                         "1. Click 'Load Image' to select an image file containing a barcode\n" +
                         "2. Click 'Scan Barcode' to detect and decode the barcode\n" +
                         "3. Results will appear in the bottom panel\n\n" +
                         "Supported image formats: JPG, PNG, GIF, BMP\n\n" +
                         "Supported barcode formats:\n" +
                         "• QR Code\n• Code 128\n• Code 39\n• EAN-13\n• UPC-A\n" +
                         "• Data Matrix\n• PDF417\n• And many more!\n\n" +
                         "Tips for best results:\n" +
                         "• Use high-resolution images\n" +
                         "• Ensure good contrast (black barcode on white background)\n" +
                         "• Avoid rotated or skewed barcodes\n" +
                         "• Make sure the barcode is clearly visible";
        
        JOptionPane.showMessageDialog(this, helpText, "Help", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateStatus(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("<html><div style='text-align: center;'>" + message + "</div></html>");
            statusLabel.setForeground(color);
        });
    }
    
    public static void main(String[] args) {
        System.out.println("=== Barcode Scanner (Image File Version) Starting ===");
        
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("Look and feel set successfully");
        } catch (Exception e) {
            System.out.println("Using default look and feel");
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                NoWebcamBarcodeScanner scanner = new NoWebcamBarcodeScanner();
                scanner.setVisible(true);
                System.out.println("=== GUI should now be visible ===");
            } catch (Exception e) {
                System.err.println("Failed to create GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
