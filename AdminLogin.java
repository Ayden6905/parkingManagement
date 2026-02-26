/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;
/**
 *
 * @author NurqistinaAtashah
 */
import javax.swing.*;
import java.awt.*;

public class AdminLogin extends JPanel {
    private MainFrame mainFrame;
    private ParkingSystemFacade facade;

    public AdminLogin(ParkingSystemFacade facade, MainFrame mainFrame) {
        this.facade = facade;
        this.mainFrame = mainFrame;

        // Centering everything using GridBag
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Standard padding for form fields

        // Header
        JLabel title = new JLabel("Admin Login");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        // Form Inputs
        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);
        JButton btnLogin = new JButton("Login");
        JButton btnBack = new JButton("Cancel");

        // UI Grid Layout setup
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; add(title, gbc);
        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0; add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; add(userField, gbc);
        gbc.gridy = 2; gbc.gridx = 0; add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; add(passField, gbc);
        
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel();
        btnPanel.add(btnLogin);
        btnPanel.add(btnBack);
        add(btnPanel, gbc);

        // Handle Login Logic
        btnLogin.addActionListener(e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());

            // Authentication is handled in the Facade to keep this class UI-focused
            if (facade.authenticateAdmin(user, pass)) {
                // Clear fields before switching screens for security
                userField.setText("");
                passField.setText("");
                mainFrame.showPanel("AdminDashboard"); 
            } else {
                // Show popup if DB check fails
                JOptionPane.showMessageDialog(this, "Access Denied!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Go back to the main psge
        btnBack.addActionListener(e -> mainFrame.showHome());
    }
}