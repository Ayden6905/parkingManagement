/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parkingmanagement;
/**
 *
 * @author NurqistinaAtashah
 */
import javax.swing.*;
import java.awt.*;


public class MainFrame extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContainer = new JPanel(cardLayout);
    private ParkingSystemFacade facade = new ParkingSystemFacade();
    
    private EntryPanel entryPanel;
    private ExitPanel exitPanel;
  

    public MainFrame() {
        setTitle("Parking Management System");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Create the Main Menu (the screen in your image)
        JPanel homePanel = createHomePanel();
        
        entryPanel = new EntryPanel(facade, this);
        exitPanel = new ExitPanel(facade, this);
        
        // 2. Add screens to the container
        mainContainer.add(homePanel, "Home");
        mainContainer.add(new EntryPanel(facade, this), "Entry"); // Member 4 task
        mainContainer.add(new ExitPanel(facade, this), "Exit");
        // mainContainer.add(new ExitPanel(facade, this), "Exit"); 
        // Change "Admin" to "AdminDashboard"
        mainContainer.add(new AdminPanel(facade, this), "AdminDashboard");
        mainContainer.add(new AdminLogin(facade, this), "AdminLogin");

        add(mainContainer);
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        JLabel title = new JLabel("Parking Management System");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        gbc.gridy = 0; 
        panel.add(title, gbc);

        // Buttons based on your image
        JButton btnEntry = createMenuButton("Entry System");
        JButton btnAdmin = createMenuButton("Admin Login");
        JButton btnExit = createMenuButton("Exit System");

        gbc.gridy = 1; panel.add(btnEntry, gbc);
        gbc.gridy = 2; panel.add(btnAdmin, gbc);
        gbc.gridy = 3; panel.add(btnExit, gbc);

        // Navigation logic
        btnEntry.addActionListener(e -> cardLayout.show(mainContainer, "Entry"));
        btnAdmin.addActionListener(e -> showPanel("AdminLogin"));
        btnExit.addActionListener(e -> {
            exitPanel.reset();
            cardLayout.show(mainContainer, "Exit");
                });
        // Add listeners for Exit and Admin later

        return panel;
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(200, 50));
        btn.setFocusPainted(false);
        return btn;
    }

    // Method to allow panels to switch back to Home
    public void showHome() {
        cardLayout.show(mainContainer, "Home");
    }
    
    // Add this inside your MainFrame class
    public void showPanel(String panelName) {
    cardLayout.show(mainContainer, panelName);
    }

   

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}