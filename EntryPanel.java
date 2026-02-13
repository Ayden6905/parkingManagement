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
import java.util.List;

public class EntryPanel extends JPanel {
    private MainFrame mainFrame; // To allow going back to home
    private ParkingSystemFacade facade;

    public EntryPanel(ParkingSystemFacade facade, MainFrame mainFrame) {
        this.facade = facade;
        this.mainFrame = mainFrame;
        
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // --- 1. UI Components (Visuals) ---
        JLabel title = new JLabel("Vehicle Entry System");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        
        JLabel lblPlate = new JLabel("License Plate:");
        JTextField plateField = new JTextField(15);
        
        JLabel lblType = new JLabel("Vehicle Type:");
        // Matches your Enum strings exactly
        String[] types = {"Car", "Motorcycle", "SUV", "Handicapped"}; 
        JComboBox<String> typeCombo = new JComboBox<>(types);
        
        JButton btnPark = new JButton("Assign Spot & Park");
        JButton btnBack = new JButton("Back to Main Menu");

        // --- 2. Layout (Positioning) ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; 
        add(title, gbc);
        
        gbc.gridwidth = 1; 
        gbc.gridy = 1; gbc.gridx = 0; add(lblPlate, gbc);
        gbc.gridx = 1; add(plateField, gbc);
        
        gbc.gridy = 2; gbc.gridx = 0; add(lblType, gbc);
        gbc.gridx = 1; add(typeCombo, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2; 
        add(btnPark, gbc);
        
        gbc.gridy = 4; 
        add(btnBack, gbc);

        // --- 3. Button Actions (Connecting to Logic) ---
        
        // BACK BUTTON
        btnBack.addActionListener(e -> mainFrame.showHome());
       
        
          btnPark.addActionListener(e -> {
            String plate = plateField.getText().trim().toUpperCase();
            String type = (String) typeCombo.getSelectedItem();

            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter license plate.");
                return;
            }

            List<String> spots = facade.getAvailableSpots();

            if (spots.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No available spots.");
                return;
            }

            String selectedSpot = (String) JOptionPane.showInputDialog(
                    this,
                    "Select Available Spot:",
                    "Choose Spot",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    spots.toArray(),
                    spots.get(0)
            );

            if (selectedSpot != null) {
                String ticketResult = facade.handleVehicleEntry(plate, type, selectedSpot);

                JTextArea textArea = new JTextArea(ticketResult);
                JOptionPane.showMessageDialog(this,
                        new JScrollPane(textArea),
                        "Ticket Issued",
                        JOptionPane.PLAIN_MESSAGE);
            }
        });


        // Navigation back to your Main Screen
        btnBack.addActionListener(e -> mainFrame.showHome());
    }
}
