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
    private JLabel lblDebtWarning;
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
        
        JButton btnReserve = new JButton("Reserve Parking Spot");
        btnReserve.addActionListener(e -> mainFrame.showReservation());
        
        msgLabel = new JLabel(" ");
        lblDebtWarning = new JLabel("");
        lblDebtWarning.setForeground(Color.RED);
        lblDebtWarning.setFont(new Font("SansSerif", Font.BOLD, 12));

        JButton btnPark = new JButton("Assign Spot & Park");
        JButton btnBack = new JButton("Back to Main Menu");

        // --- Layout ---
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

        gbc.gridy = 4; add(lblDebtWarning, gbc);
        gbc.gridy = 5; add(btnPark, gbc);
        gbc.gridy = 6; add(btnReserve, gbc);
        gbc.gridy = 7; add(btnBack, gbc);
        gbc.gridy = 8; add(msgLabel, gbc);

        // --- Logic ---
        btnBack.addActionListener(e -> mainFrame.showHome());

        btnPark.addActionListener(e -> {
    String plate = plateField.getText().trim();
    String type = (String) typeCombo.getSelectedItem();
    boolean isHandi = handicappedCheck.isSelected();

    // 1. Get ONLY available spots with their types
    List<String> options = facade.getAvailableSpotsFor(plate, type, isHandi);

    if (options.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No vacant spots available for this vehicle type.");
        return;
    }

    // 2. The selection menu now only shows vacant spots
    String selection = (String) JOptionPane.showInputDialog(
            this, "Select a vacant spot:", "Spot Assignment", 
            JOptionPane.PLAIN_MESSAGE, null, options.toArray(), options.get(0));

    if (selection != null) {
        // Extract the ID from "F1-R1-S1 (COMPACT)"
        String actualId = selection.split(" ")[0];
        
        // 3. Perform the database 'Lock'
        if (facade.lockSpotInDatabase(actualId)) {
            Vehicle v = createVehicle(plate);
            ParkingSpot spot = ParkingLot.getInstance().findSpotById(actualId);
            
            facade.parkVehicle(v, spot, "Hourly");
            JOptionPane.showMessageDialog(this, "Successfully Parked at " + actualId);
            mainFrame.showHome();
        } else {
            // Safety popup if the spot was taken while the menu was open
            JOptionPane.showMessageDialog(this, "Error: This spot was just taken!", "Occupied", JOptionPane.ERROR_MESSAGE);
        }
    }
});
    }

    private void resetPanel() {
        plateField.setText("");
        handicappedCheck.setSelected(false);
        lblDebtWarning.setText("");
        msgLabel.setText("");
    }

    private Vehicle createVehicle(String plate) {
        String type = (String) typeCombo.getSelectedItem(); 
        boolean isHandi = handicappedCheck.isSelected();
        Vehicle v;
        if ("Motorcycle".equalsIgnoreCase(type)) v = new Motorcycle(plate, 0.0);
        else if ("SUV/Truck".equalsIgnoreCase(type)) v = new SUV(plate, 0.0);
        else v = new Car(plate, 0.0);

        if (isHandi) v.setHandicappedCardHolder(true);
        return v;
    }
}