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
    private MainFrame mainFrame;
    private ParkingSystemFacade facade;
    
    private JTextField plateField;
    private JComboBox<String> typeCombo;
    private JLabel msgLabel; 
    private JCheckBox handicappedCheck;

    public EntryPanel(ParkingSystemFacade facade, MainFrame mainFrame) {
        this.facade = facade;
        this.mainFrame = mainFrame;
        
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- UI Components ---
        JLabel title = new JLabel("Vehicle Entry System");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        
        plateField = new JTextField(15);
        typeCombo = new JComboBox<>(new String[]{"Car", "Motorcycle", "SUV/Truck"});
        handicappedCheck = new JCheckBox("Handicapped card holder");
        handicappedCheck.setBackground(Color.WHITE);
        
        msgLabel = new JLabel(" ");
        msgLabel.setForeground(Color.BLUE);
        JButton btnPark = new JButton("Assign Spot & Park");
        JButton btnBack = new JButton("Back to Main Menu");

        // --- Layout Mapping ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; 
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0; 
        add(new JLabel("License Plate:"), gbc);
        gbc.gridx = 1; 
        add(plateField, gbc);

        gbc.gridy = 2; gbc.gridx = 0; 
        add(new JLabel("Vehicle Type:"), gbc);
        gbc.gridx = 1; 
        add(typeCombo, gbc);

        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2; 
        add(handicappedCheck, gbc);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2; 
        add(btnPark, gbc);

        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2; 
        add(btnBack, gbc);

        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2; 
        add(msgLabel, gbc);

        // --- Logic ---
        btnBack.addActionListener(e -> mainFrame.showHome());

        btnPark.addActionListener(e -> {
            String plate = plateField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();
            boolean isCardHolder = handicappedCheck.isSelected();

            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter license plate.");
                return;
            }
                
            // 1. Get available spots from Facade
            List<String> optionsToShow = facade.getAvailableSpotsFor(plate, type, isCardHolder);

            if (optionsToShow == null || optionsToShow.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No valid spots available for this vehicle.");
                return;
            }

            // 2. Show Spot Selection Dialog
            String selectedSpot = (String) JOptionPane.showInputDialog(
                    this,
                    "Select an available spot for " + plate + ":",
                    "Spot Assignment",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    optionsToShow.toArray(),
                    optionsToShow.get(0)
            );

            // 3. Process Entry if a spot was selected
            if (selectedSpot != null) {
                // Assuming your Facade returns a formatted string with the Ticket details
                String ticketResult = facade.handleVehicleEntry(plate, type, selectedSpot, isCardHolder);

                if (ticketResult.startsWith("Error")) {
                    JOptionPane.showMessageDialog(this, ticketResult, "Entry Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Success! Show ticket and reset
                    displayTicket(ticketResult);
                    resetFields();
                    mainFrame.showHome();
                }
            }
        });
    }

    private void resetFields() {
        plateField.setText("");
        handicappedCheck.setSelected(false);
        typeCombo.setSelectedIndex(0);
        msgLabel.setText("Vehicle parked successfully.");
    }

    private void displayTicket(String content) {
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Ticket Issued", JOptionPane.PLAIN_MESSAGE);
    }
}