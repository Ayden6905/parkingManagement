/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingmanagement;

/**
 *
 * @author NurqistinaAtashah
 */
import javax.swing.*;
import java.awt.*;

public class EntryPanel extends JPanel {
    private MainFrame mainFrame; // To allow going back to home
    private ParkingSystemFacade facade;

    public EntryPanel(ParkingSystemFacade facade, MainFrame mainFrame) {
        this.facade = facade;
        this.mainFrame = mainFrame;
        
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE); // Matching your UI style
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // UI Components
        JLabel title = new JLabel("Vehicle Entry System");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        
        JTextField plateField = new JTextField(15);
        String[] types = {"Car", "Motorcycle", "SUV", "Handicapped"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        
        JButton btnPark = new JButton("Assign Spot & Park");
        JButton btnBack = new JButton("Back to Main Menu");

        // Layout the components
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; add(title, gbc);
        
        gbc.gridwidth = 1; gbc.gridy = 1; add(new JLabel("License Plate:"), gbc);
        gbc.gridx = 1; add(plateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; add(new JLabel("Vehicle Type:"), gbc);
        gbc.gridx = 1; add(typeCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; add(btnPark, gbc);
        gbc.gridy = 4; add(btnBack, gbc);

        // Member 4: Connect UI to Facade
        btnPark.addActionListener(e -> {
            String plate = plateField.getText();
            String type = (String) typeCombo.getSelectedItem();
            String result = facade.handleVehicleEntry(plate, type);
            JOptionPane.showMessageDialog(this, result);
        });

        // Navigation back to your Main Screen
        btnBack.addActionListener(e -> mainFrame.showHome());
    }
}
