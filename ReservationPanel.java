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
import java.util.stream.Collectors;

public class ReservationPanel extends JPanel {   
    private ParkingSystemFacade facade;
    private MainFrame mainFrame;
    private JComboBox<String> spotDropdown;
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

        //Title
        JLabel title = new JLabel("Reserve Parking Spot");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        // Input Fields
        plateField = new JTextField(15);
        spotDropdown = new JComboBox<>();
        refreshAvailableReservedSpots(); 

        SpinnerNumberModel hoursModel = new SpinnerNumberModel(2, 1, 24, 1);
        JSpinner hoursSpinner = new JSpinner(hoursModel);
        msg = new JLabel(" "); 

        //Buttons
        JButton btnCreate = new JButton("Create Reservation");
        JButton btnBack = new JButton("Back");

        //Layout Mapping
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
            Object selectedItem = spotDropdown.getSelectedItem();

            // Validation
            if (plate.isEmpty() || selectedItem == null || selectedItem.toString().equals("No Reserved Spots Available")) {
                msg.setText("Plate and Spot Selection are required.");
                msg.setForeground(Color.RED);
                return;
            }

            String fullText = selectedItem.toString();
            String actualId = fullText.split(" ")[0]; 

            ParkingSpot spot = ParkingLot.getInstance().findSpotById(actualId);
            
            int hours = (Integer) hoursSpinner.getValue();
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.plusHours(hours);
            String resId = "R-" + plate + "-" + System.currentTimeMillis();

            Reservation r = new Reservation(
                    resId, plate, (ReservedSpot) spot, start, end, ReservationStatus.ACTIVE
            );
            
            ParkingRepository repo = new ParkingRepository();
            if (repo.createReservation(r)) {
                spot.setStatus(SpotStatus.OCCUPIED); 
                ParkingLot.getInstance().addReservation(r);
                
                msg.setText("Success! Spot " + actualId + " reserved for " + plate);
                msg.setForeground(new Color(0, 153, 0)); 
                
                plateField.setText("");
                refreshAvailableReservedSpots(); 
            } else {
                msg.setText("Database Error: Could not save reservation.");
                msg.setForeground(Color.RED);
            }
        });
    }

    public void refreshAvailableReservedSpots() {
        spotDropdown.removeAllItems();
        
        List<String> availableReserved = ParkingLot.getInstance().getSpots().values().stream()
            .filter(s -> s instanceof ReservedSpot)
            .filter(s -> s.getStatus() == SpotStatus.AVAILABLE)
            .map(ParkingSpot::toString) 
            .collect(Collectors.toList());

        if (availableReserved.isEmpty()) {
            spotDropdown.addItem("No Reserved Spots Available");
            spotDropdown.setEnabled(false);
        } else {
            spotDropdown.setEnabled(true);
            availableReserved.forEach(spotDropdown::addItem);
        }
    }
}