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
        // Requirement: SUV/Truck as one category
        typeCombo = new JComboBox<>(new String[]{"Car", "Motorcycle", "SUV/Truck"});
        handicappedCheck = new JCheckBox("Handicapped card holder");
        handicappedCheck.setBackground(Color.WHITE);
        
        msgLabel = new JLabel(" ");
        JButton btnPark = new JButton("Assign Spot & Park");
        JButton btnBack = new JButton("Back to Main Menu");

        // --- Layout ---
       // GridY 0: Title
gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; 
add(title, gbc);

// GridY 1: Plate
gbc.gridwidth = 1;
gbc.gridy = 1; gbc.gridx = 0; 
add(new JLabel("License Plate:"), gbc);
gbc.gridx = 1; 
add(plateField, gbc);

// GridY 2: Type
gbc.gridy = 2; gbc.gridx = 0; 
add(new JLabel("Vehicle Type:"), gbc);
gbc.gridx = 1; 
add(typeCombo, gbc);

// GridY 3: Handicapped Checkbox
gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2; 
add(handicappedCheck, gbc);

// GridY 4: Assign Spot Button
gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2; 
add(btnPark, gbc);

// GridY 5: Back Button
gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2; 
add(btnBack, gbc);

// GridY 6: Status Message
gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2; 
add(msgLabel, gbc);
        // --- Button Logic ---
        btnBack.addActionListener(e -> mainFrame.showHome());

        btnPark.addActionListener(e -> {
            String plate = plateField.getText().trim();
            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter license plate!");
                return;
            }

            // Create vehicle for filtering
            Vehicle v = createVehicle(plate);
            
            // This method in ParkingLot handles the Reserved + Type logic
            List<ParkingSpot> options = ParkingLot.getInstance().getAvailableAndReservedSpots(v, plate);

            if (options.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No valid spots available for this vehicle.");
                return;
            }

            // Convert spots to "ID (Type)" strings for the pop-up
            String[] spotStrings = options.stream()
                    .map(s -> ParkingLot.getInstance().getFormattedSpotName(s))
                    .toArray(String[]::new);

            // THE SMALL PANEL (Selection Dialog)
            String selection = (String) JOptionPane.showInputDialog(
                    this,
                    "Select a spot for " + plate + ":",
                    "Spot Assignment",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    spotStrings,
                    spotStrings[0]
            );

            if (selection != null) {
                String actualId = selection.split(" ")[0];
                ParkingSpot spot = ParkingLot.getInstance().findSpotById(actualId);
                if (spot != null) {
                    facade.parkVehicle(v, spot, "Hourly");
                    msgLabel.setText("Parked Successfully at " + actualId);
                    plateField.setText("");
                }
            }
        });
    }
    

private Vehicle createVehicle(String plate) {
        String type = (String) typeCombo.getSelectedItem(); 
        boolean isHandi = handicappedCheck.isSelected();
        Vehicle v;

        if ("Motorcycle".equalsIgnoreCase(type)) {
            v = new Motorcycle(plate, 0.0);
        } else if ("SUV/Truck".equalsIgnoreCase(type)) {
            v = new SUV(plate, 0.0);
        } else {
            v = new Car(plate, 0.0);
        }

        if (isHandi) {
            v.setHandicappedCardHolder(true);
        }
        return v;
    }
    }

