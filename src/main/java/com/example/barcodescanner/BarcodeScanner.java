package com.example.barcodescanner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Main JavaFX application for barcode scanning
 */
public class BarcodeScanner extends Application {
    
    private CameraService cameraService;
    private BarcodeDetector barcodeDetector;
    private ImageView cameraView;
    private TextArea resultArea;
    private Button scanButton;
    private Button stopButton;
    private Label statusLabel;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Barcode Scanner");
        
        // Initialize services
        cameraService = new CameraService();
        barcodeDetector = new BarcodeDetector();
        
        // Create UI components
        createUI(primaryStage);
        
        // Set up barcode detection callback
        barcodeDetector.setBarcodeDetectedCallback(this::onBarcodeDetected);
        
        primaryStage.setOnCloseRequest(e -> {
            cleanup();
            Platform.exit();
        });
        
        primaryStage.show();
    }
    
    private void createUI(Stage primaryStage) {
        // Main layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // Camera view
        cameraView = new ImageView();
        cameraView.setFitWidth(640);
        cameraView.setFitHeight(480);
        cameraView.setPreserveRatio(true);
        cameraView.setStyle("-fx-border-color: black; -fx-border-width: 2;");
        
        // Control panel
        VBox controlPanel = createControlPanel();
        
        // Result area
        VBox resultPanel = createResultPanel();
        
        // Layout arrangement
        root.setCenter(cameraView);
        root.setRight(controlPanel);
        root.setBottom(resultPanel);
        
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
    }
    
    private VBox createControlPanel() {
        VBox controlPanel = new VBox(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setAlignment(Pos.TOP_CENTER);
        controlPanel.setPrefWidth(200);
        
        Label title = new Label("Barcode Scanner");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        scanButton = new Button("Start Scanning");
        scanButton.setPrefWidth(150);
        scanButton.setOnAction(e -> startScanning());
        
        stopButton = new Button("Stop Scanning");
        stopButton.setPrefWidth(150);
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> stopScanning());
        
        statusLabel = new Label("Ready to scan");
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-text-fill: green;");
        
        Button clearButton = new Button("Clear Results");
        clearButton.setPrefWidth(150);
        clearButton.setOnAction(e -> clearResults());
        
        controlPanel.getChildren().addAll(
            title,
            new Separator(),
            scanButton,
            stopButton,
            new Separator(),
            statusLabel,
            new Separator(),
            clearButton
        );
        
        return controlPanel;
    }
    
    private VBox createResultPanel() {
        VBox resultPanel = new VBox(5);
        resultPanel.setPadding(new Insets(10));
        
        Label resultLabel = new Label("Scan Results:");
        resultLabel.setStyle("-fx-font-weight: bold;");
        
        resultArea = new TextArea();
        resultArea.setPrefHeight(150);
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPromptText("Scanned barcodes will appear here...");
        
        resultPanel.getChildren().addAll(resultLabel, resultArea);
        return resultPanel;
    }
    
    private void startScanning() {
        try {
            updateStatus("Starting camera...", "blue");
            cameraService.startCamera(cameraView);
            barcodeDetector.startDetection(cameraService);
            
            scanButton.setDisable(true);
            stopButton.setDisable(false);
            updateStatus("Scanning for barcodes...", "green");
            
        } catch (Exception e) {
            updateStatus("Error starting camera: " + e.getMessage(), "red");
            showErrorAlert("Camera Error", "Failed to start camera: " + e.getMessage());
        }
    }
    
    private void stopScanning() {
        barcodeDetector.stopDetection();
        cameraService.stopCamera();
        
        scanButton.setDisable(false);
        stopButton.setDisable(true);
        updateStatus("Scanning stopped", "orange");
    }
    
    private void onBarcodeDetected(String barcodeValue, String format) {
        Platform.runLater(() -> {
            String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
            );
            String result = String.format("[%s] %s: %s\n", timestamp, format, barcodeValue);
            resultArea.appendText(result);
            updateStatus("Barcode detected: " + format, "green");
        });
    }
    
    private void clearResults() {
        resultArea.clear();
        updateStatus("Results cleared", "blue");
    }
    
    private void updateStatus(String message, String color) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-text-fill: " + color + ";");
        });
    }
    
    private void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private void cleanup() {
        if (barcodeDetector != null) {
            barcodeDetector.stopDetection();
        }
        if (cameraService != null) {
            cameraService.stopCamera();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
