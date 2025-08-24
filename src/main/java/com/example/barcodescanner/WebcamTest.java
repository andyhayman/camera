package com.example.barcodescanner;

/**
 * Simple webcam test to check if camera access works
 */
public class WebcamTest {
    
    public static void main(String[] args) {
        System.out.println("=== WEBCAM TEST ===");
        System.out.println();
        
        try {
            // Test 1: Check if webcam class can be loaded
            System.out.println("Step 1: Loading webcam class...");
            Class<?> webcamClass = Class.forName("com.github.sarxos.webcam.Webcam");
            System.out.println("✓ Webcam class loaded successfully");
            
            // Test 2: Try to get default webcam
            System.out.println("\nStep 2: Getting default webcam...");
            Object webcam = webcamClass.getMethod("getDefault").invoke(null);
            
            if (webcam == null) {
                System.out.println("❌ No webcam found (getDefault returned null)");
                System.out.println("\nPossible causes:");
                System.out.println("1. No camera connected");
                System.out.println("2. Camera in use by another app");
                System.out.println("3. Camera permissions disabled");
                System.out.println("4. Camera drivers missing");
                
                // Try to get list of webcams
                System.out.println("\nStep 3: Checking for any available webcams...");
                try {
                    Object webcamList = webcamClass.getMethod("getWebcams").invoke(null);
                    if (webcamList instanceof java.util.List) {
                        java.util.List<?> list = (java.util.List<?>) webcamList;
                        System.out.println("Found " + list.size() + " webcam(s) in system");
                        if (list.size() > 0) {
                            System.out.println("Webcams exist but default is null - this suggests a driver or permission issue");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error getting webcam list: " + e.getMessage());
                }
                
                return;
            }
            
            System.out.println("✓ Default webcam found: " + webcam.toString());
            
            // Test 3: Get webcam name
            System.out.println("\nStep 3: Getting webcam information...");
            String name = (String) webcamClass.getMethod("getName").invoke(webcam);
            System.out.println("✓ Camera name: " + name);
            
            // Test 4: Check if webcam is open
            System.out.println("\nStep 4: Checking webcam status...");
            Boolean isOpen = (Boolean) webcamClass.getMethod("isOpen").invoke(webcam);
            System.out.println("Camera open status: " + isOpen);
            
            // Test 5: Get available resolutions
            System.out.println("\nStep 5: Getting available resolutions...");
            java.awt.Dimension[] sizes = (java.awt.Dimension[]) webcamClass.getMethod("getViewSizes").invoke(webcam);
            System.out.println("✓ Available resolutions:");
            for (java.awt.Dimension size : sizes) {
                System.out.println("  - " + size.width + "x" + size.height);
            }
            
            // Test 6: Try to open webcam
            System.out.println("\nStep 6: Attempting to open webcam...");
            Boolean opened = (Boolean) webcamClass.getMethod("open").invoke(webcam);
            
            if (opened) {
                System.out.println("✓ Webcam opened successfully!");
                
                // Test 7: Try to capture an image
                System.out.println("\nStep 7: Testing image capture...");
                Object image = webcamClass.getMethod("getImage").invoke(webcam);
                
                if (image != null) {
                    System.out.println("✓ Image captured successfully!");
                    if (image instanceof java.awt.image.BufferedImage) {
                        java.awt.image.BufferedImage bufferedImage = (java.awt.image.BufferedImage) image;
                        System.out.println("✓ Image size: " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight());
                    }
                    System.out.println("✓ WEBCAM IS FULLY FUNCTIONAL!");
                } else {
                    System.out.println("❌ Image capture returned null");
                }
                
                // Close webcam
                System.out.println("\nStep 8: Closing webcam...");
                webcamClass.getMethod("close").invoke(webcam);
                System.out.println("✓ Webcam closed successfully");
                
            } else {
                System.out.println("❌ Failed to open webcam");
                System.out.println("\nPossible solutions:");
                System.out.println("1. Close other camera applications (Skype, Teams, Zoom, etc.)");
                System.out.println("2. Check Windows camera privacy settings");
                System.out.println("3. Run as Administrator");
                System.out.println("4. Restart the computer");
            }
            
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Webcam library not found: " + e.getMessage());
            System.out.println("Missing file: webcam-capture-0.3.12.jar");
            
        } catch (NoClassDefFoundError e) {
            System.out.println("❌ Missing dependency: " + e.getMessage());
            if (e.getMessage().contains("slf4j")) {
                System.out.println("\nSOLUTION: Missing SLF4J logging library");
                System.out.println("Need to download:");
                System.out.println("- slf4j-api-1.7.36.jar");
                System.out.println("- slf4j-simple-1.7.36.jar");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error during webcam test: " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage());
            
            if (e.getCause() != null) {
                System.out.println("Cause: " + e.getCause().getMessage());
            }
            
            // Print stack trace for debugging
            System.out.println("\nFull error details:");
            e.printStackTrace();
        }
        
        System.out.println("\n=== WEBCAM TEST COMPLETE ===");
    }
}
