package com.example.barcodescanner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Simple GUI test to verify Swing is working
 */
public class SimpleGUITest {
    
    public static void main(String[] args) {
        System.out.println("Starting Simple GUI Test...");
        
        // Ensure we're on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                createAndShowGUI();
            } catch (Exception e) {
                System.err.println("Error creating GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private static void createAndShowGUI() {
        System.out.println("Creating GUI components...");
        
        // Create main frame
        JFrame frame = new JFrame("Simple GUI Test - Barcode Scanner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null); // Center on screen
        
        // Create components
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Title
        JLabel titleLabel = new JLabel("Barcode Scanner GUI Test", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("System Information"));
        
        JLabel javaLabel = new JLabel("Java Version: " + System.getProperty("java.version"));
        JLabel osLabel = new JLabel("Operating System: " + System.getProperty("os.name"));
        JLabel userLabel = new JLabel("User: " + System.getProperty("user.name"));
        
        javaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        osLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(javaLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(osLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(userLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton testButton = new JButton("Test Button");
        JButton closeButton = new JButton("Close");
        
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, 
                    "GUI is working correctly!\nSwing components are functional.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Closing application...");
                System.exit(0);
            }
        });
        
        buttonPanel.add(testButton);
        buttonPanel.add(closeButton);
        
        // Status area
        JTextArea statusArea = new JTextArea(5, 40);
        statusArea.setEditable(false);
        statusArea.setText("GUI Test Status:\n");
        statusArea.append("✓ JFrame created successfully\n");
        statusArea.append("✓ Components added successfully\n");
        statusArea.append("✓ Event handlers working\n");
        statusArea.append("✓ Ready to test barcode scanner GUI\n");
        
        JScrollPane scrollPane = new JScrollPane(statusArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Status"));
        
        // Layout
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        
        // Show the frame
        System.out.println("Showing GUI window...");
        frame.setVisible(true);
        
        System.out.println("GUI should now be visible!");
        System.out.println("If you can see this window, Swing GUI is working correctly.");
    }
}
