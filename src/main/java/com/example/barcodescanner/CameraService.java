package com.example.barcodescanner;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service for managing camera capture and video streaming
 */
public class CameraService {
    
    private Webcam webcam;
    private Task<Void> cameraTask;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private ImageView imageView;
    private volatile BufferedImage currentFrame;
    
    /**
     * Start the camera and begin streaming to the provided ImageView
     */
    public void startCamera(ImageView imageView) throws Exception {
        if (isRunning.get()) {
            return;
        }
        
        this.imageView = imageView;
        
        // Get default webcam
        webcam = Webcam.getDefault();
        if (webcam == null) {
            throw new RuntimeException("No webcam found");
        }
        
        // Set camera resolution
        Dimension[] sizes = webcam.getViewSizes();
        Dimension selectedSize = new Dimension(640, 480);
        
        // Try to find a suitable resolution
        for (Dimension size : sizes) {
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
        
        isRunning.set(true);
        
        // Create camera streaming task
        cameraTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (isRunning.get() && !isCancelled()) {
                    try {
                        BufferedImage image = webcam.getImage();
                        if (image != null) {
                            currentFrame = image;
                            
                            // Update UI on JavaFX Application Thread
                            Platform.runLater(() -> {
                                if (imageView != null) {
                                    imageView.setImage(SwingFXUtils.toFXImage(image, null));
                                }
                            });
                        }
                        
                        // Control frame rate (approximately 30 FPS)
                        Thread.sleep(33);
                        
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        System.err.println("Error capturing frame: " + e.getMessage());
                    }
                }
                return null;
            }
        };
        
        // Start camera task in background thread
        Thread cameraThread = new Thread(cameraTask);
        cameraThread.setDaemon(true);
        cameraThread.start();
    }
    
    /**
     * Stop the camera and cleanup resources
     */
    public void stopCamera() {
        isRunning.set(false);
        
        if (cameraTask != null) {
            cameraTask.cancel(true);
        }
        
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
        
        currentFrame = null;
        
        // Clear the image view
        if (imageView != null) {
            Platform.runLater(() -> imageView.setImage(null));
        }
    }
    
    /**
     * Get the current frame from the camera
     */
    public BufferedImage getCurrentFrame() {
        return currentFrame;
    }
    
    /**
     * Check if the camera is currently running
     */
    public boolean isRunning() {
        return isRunning.get();
    }
    
    /**
     * Get information about available webcams
     */
    public static String[] getAvailableWebcams() {
        return Webcam.getWebcams().stream()
                .map(Webcam::getName)
                .toArray(String[]::new);
    }
    
    /**
     * Check if any webcam is available
     */
    public static boolean isWebcamAvailable() {
        return Webcam.getDefault() != null;
    }
}
