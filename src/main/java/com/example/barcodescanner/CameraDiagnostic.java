package com.example.barcodescanner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Camera diagnostic tool to help identify camera access issues
 */
public class CameraDiagnostic extends JFrame {
    
    private JTextArea outputArea;
    private JButton testButton;
    private JButton refreshButton;
    
    public CameraDiagnostic() {
        setTitle("Camera Diagnostic Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        setupUI();
        runInitialDiagnostics();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Title
        JLabel titleLabel = new JLabel("Camera Diagnostic Tool", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setBackground(Color.BLACK);
        outputArea.setForeground(Color.GREEN);
        
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        testButton = new JButton("Test Camera Access");
        refreshButton = new JButton("Refresh Diagnostics");
        JButton closeButton = new JButton("Close");
        
        testButton.addActionListener(e -> testCameraAccess());
        refreshButton.addActionListener(e -> runInitialDiagnostics());
        closeButton.addActionListener(e -> System.exit(0));
        
        buttonPanel.add(testButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        // Layout
        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void runInitialDiagnostics() {
        outputArea.setText("");
        log("=== CAMERA DIAGNOSTIC TOOL ===");
        log("");
        
        // System information
        log("SYSTEM INFORMATION:");
        log("Java Version: " + System.getProperty("java.version"));
        log("Java Vendor: " + System.getProperty("java.vendor"));
        log("OS Name: " + System.getProperty("os.name"));
        log("OS Version: " + System.getProperty("os.version"));
        log("OS Architecture: " + System.getProperty("os.arch"));
        log("User: " + System.getProperty("user.name"));
        log("");
        
        // Check Java AWT/Swing
        log("GUI SYSTEM CHECK:");
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            if (ge.isHeadlessInstance()) {
                log("❌ ERROR: Running in headless mode (no GUI support)");
            } else {
                log("✓ GUI environment available");
                GraphicsDevice[] devices = ge.getScreenDevices();
                log("✓ Screen devices found: " + devices.length);
            }
        } catch (Exception e) {
            log("❌ ERROR checking GUI: " + e.getMessage());
        }
        log("");
        
        // Check webcam library availability
        log("WEBCAM LIBRARY CHECK:");
        try {
            Class<?> webcamClass = Class.forName("com.github.sarxos.webcam.Webcam");
            log("✓ Webcam capture library found");
            
            // Check for required dependencies
            try {
                Class.forName("org.slf4j.Logger");
                log("✓ SLF4J logging library found");
            } catch (ClassNotFoundException e) {
                log("❌ SLF4J logging library missing");
                log("   This is likely causing the camera access issue!");
                log("   Solution: Download slf4j-api and slf4j-simple JAR files");
            }
            
        } catch (ClassNotFoundException e) {
            log("❌ Webcam capture library not found");
            log("   File missing: webcam-capture-0.3.12.jar");
        }
        log("");
        
        // Check available JAR files
        log("DEPENDENCY CHECK:");
        java.io.File libDir = new java.io.File("lib");
        if (libDir.exists() && libDir.isDirectory()) {
            java.io.File[] jarFiles = libDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jarFiles != null && jarFiles.length > 0) {
                log("JAR files found in lib directory:");
                for (java.io.File jar : jarFiles) {
                    log("  ✓ " + jar.getName() + " (" + jar.length() + " bytes)");
                }
            } else {
                log("❌ No JAR files found in lib directory");
            }
        } else {
            log("❌ lib directory not found");
        }
        log("");
        
        // Windows-specific camera checks
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            log("WINDOWS CAMERA CHECKS:");
            log("• Check Windows Camera Privacy Settings:");
            log("  Settings > Privacy > Camera > Allow apps to access camera");
            log("• Check if other apps are using the camera:");
            log("  Close Skype, Teams, Zoom, etc.");
            log("• Try Windows Camera app to test if camera works");
            log("");
        }
        
        log("Click 'Test Camera Access' to attempt camera initialization...");
    }
    
    private void testCameraAccess() {
        log("");
        log("=== TESTING CAMERA ACCESS ===");
        
        try {
            // Test 1: Load webcam class
            log("Step 1: Loading webcam class...");
            Class<?> webcamClass = Class.forName("com.github.sarxos.webcam.Webcam");
            log("✓ Webcam class loaded successfully");
            
            // Test 2: Get default webcam
            log("Step 2: Getting default webcam...");
            Object webcam = webcamClass.getMethod("getDefault").invoke(null);
            
            if (webcam == null) {
                log("❌ PROBLEM FOUND: getDefault() returned null");
                log("");
                log("POSSIBLE CAUSES:");
                log("1. No camera is connected to the computer");
                log("2. Camera drivers are not installed");
                log("3. Camera is being used by another application");
                log("4. Windows camera permissions are disabled");
                log("5. Missing native libraries for camera access");
                log("");
                log("SOLUTIONS TO TRY:");
                log("1. Connect a USB webcam or enable built-in camera");
                log("2. Update camera drivers in Device Manager");
                log("3. Close all other camera applications (Skype, Teams, etc.)");
                log("4. Check Windows Privacy Settings > Camera");
                log("5. Try running as Administrator");
                log("6. Restart the computer");
                return;
            }
            
            log("✓ Default webcam found: " + webcam.toString());
            
            // Test 3: Get webcam name
            log("Step 3: Getting webcam information...");
            String name = (String) webcamClass.getMethod("getName").invoke(webcam);
            log("✓ Camera name: " + name);
            
            // Test 4: Get available resolutions
            log("Step 4: Getting available resolutions...");
            java.awt.Dimension[] sizes = (java.awt.Dimension[]) webcamClass.getMethod("getViewSizes").invoke(webcam);
            log("✓ Available resolutions:");
            for (java.awt.Dimension size : sizes) {
                log("  - " + size.width + "x" + size.height);
            }
            
            // Test 5: Try to open camera
            log("Step 5: Attempting to open camera...");
            Boolean opened = (Boolean) webcamClass.getMethod("open").invoke(webcam);
            
            if (opened) {
                log("✓ Camera opened successfully!");
                
                // Test 6: Try to capture an image
                log("Step 6: Testing image capture...");
                Object image = webcamClass.getMethod("getImage").invoke(webcam);
                if (image != null) {
                    log("✓ Image captured successfully!");
                    log("✓ Camera is fully functional!");
                } else {
                    log("⚠ Camera opened but image capture failed");
                }
                
                // Close camera
                webcamClass.getMethod("close").invoke(webcam);
                log("✓ Camera closed");
                
            } else {
                log("❌ PROBLEM: Camera failed to open");
                log("");
                log("POSSIBLE CAUSES:");
                log("1. Camera is in use by another application");
                log("2. Insufficient permissions");
                log("3. Camera hardware issue");
                log("");
                log("SOLUTIONS:");
                log("1. Close all other camera applications");
                log("2. Run as Administrator");
                log("3. Check Device Manager for camera issues");
            }
            
        } catch (ClassNotFoundException e) {
            log("❌ ERROR: Webcam library not found");
            log("Missing: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            log("❌ ERROR: Webcam library method not found");
            log("Issue: " + e.getMessage());
        } catch (Exception e) {
            log("❌ ERROR during camera test: " + e.getClass().getSimpleName());
            log("Message: " + e.getMessage());
            
            if (e.getCause() != null) {
                log("Cause: " + e.getCause().getMessage());
            }
            
            // Check for specific error types
            if (e.getMessage() != null) {
                String msg = e.getMessage().toLowerCase();
                if (msg.contains("slf4j")) {
                    log("");
                    log("SOLUTION: This is an SLF4J logging library issue");
                    log("Download these files to the lib directory:");
                    log("- slf4j-api-1.7.36.jar");
                    log("- slf4j-simple-1.7.36.jar");
                } else if (msg.contains("permission")) {
                    log("");
                    log("SOLUTION: This is a permissions issue");
                    log("1. Check Windows camera privacy settings");
                    log("2. Try running as Administrator");
                } else if (msg.contains("native")) {
                    log("");
                    log("SOLUTION: This is a native library issue");
                    log("1. Install camera drivers");
                    log("2. Try a different webcam");
                }
            }
        }
        
        log("");
        log("=== DIAGNOSTIC COMPLETE ===");
    }
    
    private void log(String message) {
        outputArea.append(message + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Use default look and feel
            }
            
            new CameraDiagnostic().setVisible(true);
        });
    }
}
