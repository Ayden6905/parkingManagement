/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


 // MEMBER 4 UI + MEMBER 2 LOGIC CONNECTION 
public class EntryPanel extends JPanel {
    private MainFrame mainFrame; 
    private ParkingSystemFacade facade; // The bridge to logic

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

        //Button Actions 
        // BACK BUTTON
        btnBack.addActionListener(e -> mainFrame.showHome());

        // PARK BUTTON (Critical)
        btnPark.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get Input from UI
                String plate = plateField.getText();
                String type = (String) typeCombo.getSelectedItem();
                
                // Call Facade (Member 2 Logic)
                String result = facade.handleVehicleEntry(plate, type);
                
                // Show Result to User
                JOptionPane.showMessageDialog(EntryPanel.this, result);
                
                // Clear text field if successful
                if (result.startsWith("Success")) {
                    plateField.setText("");
                }
            }
        });
    }
}