package com.example.barcodescanner;

import com.github.sarxos.webcam.Webcam;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Console-based barcode scanner for testing without JavaFX
 */
public class ConsoleBarcodeTest {
    
    private static final MultiFormatReader reader = new MultiFormatReader();
    
    static {
        // Configure barcode reader
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.values());
        reader.setHints(hints);
    }
    
    public static void main(String[] args) {
        System.out.println("=== Barcode Scanner Console Test ===");
        System.out.println();
        
        // Test 1: Check if webcam is available
        System.out.println("1. Testing webcam availability...");
        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            System.out.println("❌ ERROR: No webcam found!");
            System.out.println("Please ensure your webcam is connected and not being used by another application.");
            return;
        }
        System.out.println("✅ Webcam found: " + webcam.getName());
        
        // Test 2: Check webcam capabilities
        System.out.println("\n2. Testing webcam capabilities...");
        Dimension[] sizes = webcam.getViewSizes();
        System.out.println("Available resolutions:");
        for (Dimension size : sizes) {
            System.out.println("   - " + size.width + "x" + size.height);
        }
        
        // Test 3: Try to open webcam
        System.out.println("\n3. Testing webcam access...");
        try {
            webcam.setViewSize(new Dimension(640, 480));
            if (!webcam.open()) {
                System.out.println("❌ ERROR: Failed to open webcam!");
                return;
            }
            System.out.println("✅ Webcam opened successfully");
        } catch (Exception e) {
            System.out.println("❌ ERROR: Exception opening webcam: " + e.getMessage());
            return;
        }
        
        // Test 4: Capture and analyze frames
        System.out.println("\n4. Testing barcode detection...");
        System.out.println("Point a barcode at your camera. Scanning for 30 seconds...");
        System.out.println("Press Ctrl+C to stop early.");
        
        long startTime = System.currentTimeMillis();
        long lastDetectionTime = 0;
        String lastDetectedBarcode = "";
        int frameCount = 0;
        int detectionCount = 0;
        
        try {
            while (System.currentTimeMillis() - startTime < 30000) { // 30 seconds
                BufferedImage image = webcam.getImage();
                if (image != null) {
                    frameCount++;
                    
                    // Try to detect barcode
                    try {
                        LuminanceSource source = new BufferedImageLuminanceSource(image);
                        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                        Result result = reader.decode(bitmap);
                        
                        String barcodeText = result.getText();
                        String format = result.getBarcodeFormat().toString();
                        
                        // Avoid duplicate detections
                        long currentTime = System.currentTimeMillis();
                        if (!barcodeText.equals(lastDetectedBarcode) || 
                            (currentTime - lastDetectionTime) > 3000) {
                            
                            detectionCount++;
                            lastDetectedBarcode = barcodeText;
                            lastDetectionTime = currentTime;
                            
                            System.out.println("\n🎯 BARCODE DETECTED!");
                            System.out.println("   Format: " + format);
                            System.out.println("   Value:  " + barcodeText);
                            System.out.println("   Time:   " + new java.util.Date());
                            System.out.println();
                        }
                        
                    } catch (NotFoundException e) {
                        // No barcode found in this frame - this is normal
                    }
                }
                
                // Show progress every 5 seconds
                if (frameCount % 150 == 0) { // Approximately every 5 seconds at 30fps
                    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                    System.out.println("Scanning... " + elapsed + "s elapsed, " + frameCount + " frames processed");
                }
                
                Thread.sleep(33); // ~30 FPS
            }
            
        } catch (InterruptedException e) {
            System.out.println("\nScan interrupted by user.");
        } catch (Exception e) {
            System.out.println("❌ ERROR during scanning: " + e.getMessage());
        } finally {
            webcam.close();
        }
        
        // Test results
        System.out.println("\n=== TEST RESULTS ===");
        System.out.println("Frames processed: " + frameCount);
        System.out.println("Barcodes detected: " + detectionCount);
        System.out.println("Webcam: ✅ Working");
        System.out.println("Barcode detection: " + (detectionCount > 0 ? "✅ Working" : "⚠️  No barcodes detected"));
        
        if (detectionCount == 0) {
            System.out.println("\nTips for better detection:");
            System.out.println("- Ensure good lighting");
            System.out.println("- Hold barcode steady and at proper distance");
            System.out.println("- Try different angles");
            System.out.println("- Use high-contrast barcodes (black on white)");
        }
        
        System.out.println("\nTest completed!");
    }
}
