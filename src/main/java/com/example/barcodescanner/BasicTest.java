package com.example.barcodescanner;

import com.github.sarxos.webcam.Webcam;
import com.google.zxing.BarcodeFormat;

/**
 * Basic test to verify dependencies are working
 */
public class BasicTest {
    
    public static void main(String[] args) {
        System.out.println("=== Basic Barcode Scanner Test ===");
        System.out.println();
        
        // Test 1: Check ZXing library
        System.out.println("1. Testing ZXing library...");
        try {
            BarcodeFormat[] formats = BarcodeFormat.values();
            System.out.println("✅ ZXing library loaded successfully");
            System.out.println("   Supported formats: " + formats.length);
            for (BarcodeFormat format : formats) {
                System.out.println("   - " + format.name());
            }
        } catch (Exception e) {
            System.out.println("❌ ERROR: ZXing library failed: " + e.getMessage());
            return;
        }
        
        System.out.println();
        
        // Test 2: Check Webcam Capture library
        System.out.println("2. Testing Webcam Capture library...");
        try {
            Webcam[] webcams = Webcam.getWebcams().toArray(new Webcam[0]);
            System.out.println("✅ Webcam Capture library loaded successfully");
            System.out.println("   Available webcams: " + webcams.length);
            
            if (webcams.length == 0) {
                System.out.println("⚠️  No webcams detected");
                System.out.println("   This could mean:");
                System.out.println("   - No webcam is connected");
                System.out.println("   - Webcam is being used by another application");
                System.out.println("   - Driver issues");
                System.out.println("   - Permission issues");
            } else {
                for (int i = 0; i < webcams.length; i++) {
                    System.out.println("   " + (i + 1) + ". " + webcams[i].getName());
                }
            }
        } catch (Exception e) {
            System.out.println("❌ ERROR: Webcam Capture library failed: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        System.out.println();
        
        // Test 3: Check Java version compatibility
        System.out.println("3. Testing Java compatibility...");
        String javaVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        System.out.println("✅ Java version: " + javaVersion);
        System.out.println("   Java vendor: " + javaVendor);
        
        // Check if Java version is compatible
        try {
            String[] versionParts = javaVersion.split("\\.");
            int majorVersion = Integer.parseInt(versionParts[0]);
            if (majorVersion >= 11) {
                System.out.println("✅ Java version is compatible (11+)");
            } else {
                System.out.println("⚠️  Java version might be too old (recommended: 11+)");
            }
        } catch (Exception e) {
            System.out.println("⚠️  Could not parse Java version");
        }
        
        System.out.println();
        
        // Test 4: Check system properties
        System.out.println("4. System information...");
        System.out.println("   OS: " + System.getProperty("os.name"));
        System.out.println("   Architecture: " + System.getProperty("os.arch"));
        System.out.println("   User: " + System.getProperty("user.name"));
        
        System.out.println();
        System.out.println("=== TEST SUMMARY ===");
        System.out.println("✅ Dependencies loaded successfully");
        System.out.println("✅ Basic functionality test passed");
        System.out.println();
        System.out.println("Next steps:");
        System.out.println("1. If webcams were detected, try the camera test");
        System.out.println("2. If no webcams detected, check camera connections and permissions");
        System.out.println("3. Try running the full GUI application");
    }
}
