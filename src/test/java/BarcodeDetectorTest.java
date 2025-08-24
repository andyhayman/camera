import com.example.barcodescanner.BarcodeDetector;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BarcodeDetector
 */
public class BarcodeDetectorTest {
    
    private BarcodeDetector barcodeDetector;
    
    @BeforeEach
    void setUp() {
        barcodeDetector = new BarcodeDetector();
    }
    
    @Test
    void testBarcodeDetectorInitialization() {
        assertNotNull(barcodeDetector);
        assertFalse(barcodeDetector.isDetecting());
    }
    
    @Test
    void testSupportedFormats() {
        BarcodeFormat[] formats = BarcodeDetector.getSupportedFormats();
        assertNotNull(formats);
        assertTrue(formats.length > 0);
        
        // Check that common formats are supported
        boolean hasQRCode = false;
        boolean hasCode128 = false;
        
        for (BarcodeFormat format : formats) {
            if (format == BarcodeFormat.QR_CODE) {
                hasQRCode = true;
            }
            if (format == BarcodeFormat.CODE_128) {
                hasCode128 = true;
            }
        }
        
        assertTrue(hasQRCode, "QR Code format should be supported");
        assertTrue(hasCode128, "Code 128 format should be supported");
    }
    
    @Test
    void testCallbackSetting() {
        boolean[] callbackCalled = {false};
        
        barcodeDetector.setBarcodeDetectedCallback((value, format) -> {
            callbackCalled[0] = true;
        });
        
        // The callback should be set without throwing an exception
        assertDoesNotThrow(() -> {
            barcodeDetector.setBarcodeDetectedCallback((value, format) -> {});
        });
    }
    
    @Test
    void testDetectionState() {
        assertFalse(barcodeDetector.isDetecting());
        
        // Stop detection should work even when not started
        assertDoesNotThrow(() -> barcodeDetector.stopDetection());
        
        assertFalse(barcodeDetector.isDetecting());
    }
}
