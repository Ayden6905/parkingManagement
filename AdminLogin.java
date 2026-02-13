/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

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

        setLayout(new GridBagLayout());
        setBackground(Color.WHITE); // Matches the clean look of your design
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // UI Components
        JLabel title = new JLabel("Admin Login");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);
        JButton btnLogin = new JButton("Login");
        JButton btnBack = new JButton("Cancel");

        // Layout Components
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

        // Login Action
        btnLogin.addActionListener(e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());

            // Delegate verification to the Facade
            if (facade.authenticateAdmin(user, pass)) {
                userField.setText("");
                passField.setText("");
                mainFrame.showPanel("AdminDashboard"); // Move to actual Admin Panel
            } else {
                JOptionPane.showMessageDialog(this, "Access Denied!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBack.addActionListener(e -> mainFrame.showHome());
    }
}