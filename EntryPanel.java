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
    private MainFrame mainFrame; //to go back home
    private ParkingSystemFacade facade;
    
    private JTextField plateField;
    private JComboBox<String> typeCombo;
    private JLabel lblDebtWarning;

    public EntryPanel(ParkingSystemFacade facade, MainFrame mainFrame) {
        this.facade = facade;
        this.mainFrame = mainFrame;
        
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        //1. UI Components
        JLabel title = new JLabel("Vehicle Entry System");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        
        JLabel lblPlate = new JLabel("License Plate:");
        plateField = new JTextField(15);
        
        JLabel lblType = new JLabel("Vehicle Type:");
        String[] types = {"Car", "Motorcycle", "SUV", "Handicapped"}; 
        typeCombo = new JComboBox<>(types);
        
        JButton btnPark = new JButton("Assign Spot & Park");
        JButton btnBack = new JButton("Back to Main Menu");

        lblDebtWarning = new JLabel("");
        lblDebtWarning.setForeground(Color.RED);
        lblDebtWarning.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        
        // Layout the components
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; 
        add(title, gbc);
        
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0; add(lblPlate, gbc);
        gbc.gridx = 1; add(plateField, gbc);

        gbc.gridy = 2; gbc.gridx = 0; add(lblType, gbc);
        gbc.gridx = 1; add(typeCombo, gbc);
        
        gbc.gridy = 5; // Put it above or below the buttons
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        add(lblDebtWarning, gbc);

        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2; add(btnPark, gbc);
        gbc.gridy = 4; add(btnBack, gbc);
        
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
            
            // --- NEW DEBT CHECK LOGIC ---
    double existingDebt = facade.checkExistingDebt(plate);
    if (existingDebt > 0) {
        int choice = JOptionPane.showConfirmDialog(this,
            "Vehicle has an outstanding fine of RM " + String.format("%.2f", existingDebt) + 
            ".\nContinue with entry?", 
            "Outstanding Debt Found", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        
        if (choice != JOptionPane.YES_OPTION) {
            return; // Block entry if operator chooses 'No'
        }
        lblDebtWarning.setText("⚠️ UNPAID FINES: RM " + String.format("%.2f", existingDebt));
    } else {
        lblDebtWarning.setText(""); // Clear if no debt
    }
    // --- END DEBT CHECK ---

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
                textArea.setEditable(false);
                JOptionPane.showMessageDialog(this,
                        new JScrollPane(textArea),
                        "Ticket Issued",
                        JOptionPane.PLAIN_MESSAGE);
                
                if (!ticketResult.startsWith("Error")) {
            plateField.setText("");
            mainFrame.showHome(); // Take them back after parking
                }
            }
        });
    }
}