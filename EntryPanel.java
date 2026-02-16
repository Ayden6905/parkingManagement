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
        // Requirement: SUV/Truck as one category
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

        // Debt Warning Label
        gbc.gridy = 4;
        add(lblDebtWarning, gbc);

        gbc.gridy = 5;
        add(btnPark, gbc);

        gbc.gridy = 6;
        add(btnReserve, gbc);

        gbc.gridy = 7;
        add(btnBack, gbc);

        gbc.gridy = 8;
        add(msgLabel, gbc);

        // --- Logic ---
        btnBack.addActionListener(e -> mainFrame.showHome());

        btnPark.addActionListener(e -> {
            String plate = plateField.getText().trim();
            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter license plate!");
                return;
            }

            // Debt Check Logic
            double existingDebt = facade.checkExistingDebt(plate);
            if (existingDebt > 0) {
                int choice = JOptionPane.showConfirmDialog(this,
                    "Vehicle has an outstanding fine of RM " + String.format("%.2f", existingDebt) + 
                    ".\nContinue with entry?", 
                    "Outstanding Debt Found", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.WARNING_MESSAGE);
                
                if (choice != JOptionPane.YES_OPTION) return;

                lblDebtWarning.setText("⚠️ UNPAID FINES: RM " + String.format("%.2f", existingDebt));
            } else {
                lblDebtWarning.setText(""); 
            }
            
            // Create vehicle for filtering
            Vehicle v = createVehicle(plate);
            
            // This method in ParkingLot handles the Reserved + Type logic
            List<ParkingSpot> options = ParkingLot.getInstance().getAvailableSpotsForVehicle(v);


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