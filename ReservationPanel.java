/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parkingmanagement;
/**
 *
 * @author HP
 */
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationPanel extends JPanel {   
    private ParkingSystemFacade facade;
    private MainFrame mainFrame;
    private JComboBox<String> spotDropdown; // This is the correct variable name
    private JTextField plateField;
    private JLabel msg;
    
    public ReservationPanel(ParkingSystemFacade facade, MainFrame mainFrame) {
        this.facade = facade;
        this.mainFrame = mainFrame;

        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Reserve Parking Spot");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        plateField = new JTextField(15);
        spotDropdown = new JComboBox<>();
        refreshAvailableReservedSpots();

        SpinnerNumberModel hoursModel = new SpinnerNumberModel(2, 1, 24, 1);
        JSpinner hoursSpinner = new JSpinner(hoursModel);
        msg = new JLabel(" "); 

        JButton btnCreate = new JButton("Create Reservation");
        JButton btnBack = new JButton("Back");

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; 
        add(title, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; add(new JLabel("Plate:"), gbc);
        gbc.gridx = 1; add(plateField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; add(new JLabel("Available Reserved Spots:"), gbc);
        gbc.gridx = 1; add(spotDropdown, gbc);

        gbc.gridx = 0; gbc.gridy = 3; add(new JLabel("Duration (hours):"), gbc);
        gbc.gridx = 1; add(hoursSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 4; add(btnCreate, gbc);
        gbc.gridx = 1; add(btnBack, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; 
        add(msg, gbc);
        
        btnBack.addActionListener(e -> mainFrame.showHome());

        btnCreate.addActionListener(e -> {
    String plate = plateField.getText().trim();
    String selection = (String) spotDropdown.getSelectedItem(); 
    
    if (plate.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter a license plate.");
        return;
    }

    if (selection != null && !selection.equals("No Reserved Spots Available")) {
        String actualId = selection.split(" ")[0];
        
        // This method in your Facade performs the SQL check and INSERT
        boolean success = facade.createReservationInDB(plate, actualId, LocalDateTime.now());
        
        if (success) {
            JOptionPane.showMessageDialog(this, "Reservation Successful for spot: " + actualId);
            
            // CRITICAL: Refresh immediately after success to remove the spot from the list
            refreshAvailableReservedSpots(); 
            
            plateField.setText(""); // Clear field for next use
            mainFrame.showHome();
        } else {
            // This is triggered if someone else reserved it in the split second before you
            JOptionPane.showMessageDialog(this, 
                "ERROR: Spot " + actualId + " is no longer available!", 
                "Reservation Error", 
                JOptionPane.ERROR_MESSAGE);
            
            refreshAvailableReservedSpots(); // Sync the dropdown with the DB
        }
    }
});
    }

    public void refreshAvailableReservedSpots() {
        spotDropdown.removeAllItems();
        
        // This ensures only spots that are Available and NOT currently reserved show up
        List<String> availableReserved = facade.getAvailableReservedSpotsForUI(); 

        if (availableReserved.isEmpty()) {
            spotDropdown.addItem("No Reserved Spots Available");
            spotDropdown.setEnabled(false);
        } else {
            spotDropdown.setEnabled(true);
            availableReserved.forEach(spotDropdown::addItem);
        }
    }
}