package com.example.barcodescanner;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * Service for detecting and decoding barcodes from camera frames
 */
public class BarcodeDetector {
    
    private final MultiFormatReader reader;
    private final GenericMultipleBarcodeReader multiReader;
    private final AtomicBoolean isDetecting = new AtomicBoolean(false);
    private Thread detectionThread;
    private CameraService cameraService;
    private BiConsumer<String, String> barcodeDetectedCallback;
    
    // Track last detected barcode to avoid duplicates
    private String lastDetectedBarcode = "";
    private long lastDetectionTime = 0;
    private static final long DETECTION_COOLDOWN_MS = 2000; // 2 seconds
    
    public BarcodeDetector() {
        // Initialize barcode reader
        reader = new MultiFormatReader();
        multiReader = new GenericMultipleBarcodeReader(reader);
        
        // Configure hints for better detection
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.values());
        hints.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
        
        reader.setHints(hints);
    }
    
    /**
     * Set the callback function to be called when a barcode is detected
     */
    public void setBarcodeDetectedCallback(BiConsumer<String, String> callback) {
        this.barcodeDetectedCallback = callback;
    }
    
    /**
     * Start barcode detection from camera frames
     */
    public void startDetection(CameraService cameraService) {
        if (isDetecting.get()) {
            return;
        }
        
        this.cameraService = cameraService;
        isDetecting.set(true);
        
        detectionThread = new Thread(this::detectBarcodes);
        detectionThread.setDaemon(true);
        detectionThread.start();
    }
    
    /**
     * Stop barcode detection
     */
    public void stopDetection() {
        isDetecting.set(false);
        
        if (detectionThread != null) {
            detectionThread.interrupt();
        }
    }
    
    /**
     * Main detection loop
     */
    private void detectBarcodes() {
        while (isDetecting.get() && !Thread.currentThread().isInterrupted()) {
            try {
                if (cameraService != null && cameraService.isRunning()) {
                    BufferedImage frame = cameraService.getCurrentFrame();
                    if (frame != null) {
                        detectBarcodesInFrame(frame);
                    }
                }
                
                // Control detection rate (check every 100ms)
                Thread.sleep(100);
                
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                System.err.println("Error during barcode detection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Detect barcodes in a single frame
     */
    private void detectBarcodesInFrame(BufferedImage frame) {
        try {
            // Convert image to luminance source
            LuminanceSource source = new BufferedImageLuminanceSource(frame);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
            // Try to detect multiple barcodes first
            try {
                Result[] results = multiReader.decodeMultiple(bitmap);
                if (results != null && results.length > 0) {
                    for (Result result : results) {
                        processBarcodeResult(result);
                    }
                    return;
                }
            } catch (NotFoundException e) {
                // No multiple barcodes found, try single barcode detection
            }
            
            // Try single barcode detection
            try {
                Result result = reader.decode(bitmap);
                if (result != null) {
                    processBarcodeResult(result);
                }
            } catch (NotFoundException e) {
                // No barcode found in this frame
            }
            
        } catch (Exception e) {
            // Ignore detection errors for individual frames
        }
    }
    
    /**
     * Process a detected barcode result
     */
    private void processBarcodeResult(Result result) {
        String barcodeText = result.getText();
        String format = result.getBarcodeFormat().toString();
        
        // Avoid duplicate detections
        long currentTime = System.currentTimeMillis();
        if (barcodeText.equals(lastDetectedBarcode) && 
            (currentTime - lastDetectionTime) < DETECTION_COOLDOWN_MS) {
            return;
        }
        
        lastDetectedBarcode = barcodeText;
        lastDetectionTime = currentTime;
        
        // Call the callback if set
        if (barcodeDetectedCallback != null) {
            barcodeDetectedCallback.accept(barcodeText, format);
        }
        
        System.out.println("Barcode detected: " + format + " = " + barcodeText);
    }
    
    /**
     * Detect barcode from a static image (utility method)
     */
    public Result detectBarcodeFromImage(BufferedImage image) throws NotFoundException {
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        return reader.decode(bitmap);
    }
    
    /**
     * Check if detection is currently running
     */
    public boolean isDetecting() {
        return isDetecting.get();
    }
    
    /**
     * Get supported barcode formats
     */
    public static BarcodeFormat[] getSupportedFormats() {
        return BarcodeFormat.values();
    }
}
