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

public class AdminPanel extends JPanel {
    private MainFrame mainFrame;
    private ParkingSystemFacade facade;

    public AdminPanel(ParkingSystemFacade facade, MainFrame mainFrame) {
        this.facade = facade;
        this.mainFrame = mainFrame;
        
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));

        // 1. Top Navigation Bar
        JPanel navBar = new JPanel(new BorderLayout());
        JButton btnBack = new JButton("Logout/Back");
        navBar.add(btnBack, BorderLayout.WEST);
        navBar.add(new JLabel("ADMINISTRATOR DASHBOARD", SwingConstants.CENTER), BorderLayout.CENTER);
        add(navBar, BorderLayout.NORTH);

        // 2. Center Content - Stats and Fine Management
        JPanel content = new JPanel(new GridLayout(2, 1, 10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Stats Section
        JPanel statsPanel = new JPanel(new FlowLayout());
        statsPanel.setBorder(BorderFactory.createTitledBorder("Live System Stats"));
        statsPanel.add(new JLabel("Current Occupancy: 85%")); // This will eventually call Facade
        statsPanel.add(new JLabel(" | Total Revenue: RM 450.00"));
        content.add(statsPanel);

        // Fine Scheme Section (Member 2's Logic integration)
        JPanel finePanel = new JPanel(new FlowLayout());
        finePanel.setBorder(BorderFactory.createTitledBorder("System Configuration"));
        String[] schemes = {"Standard Scheme", "Holiday Scheme", "Student Discount"};
        JComboBox<String> schemeCombo = new JComboBox<>(schemes);
        JButton btnUpdate = new JButton("Apply Fine Scheme");
        
        finePanel.add(new JLabel("Active Fine Strategy:"));
        finePanel.add(schemeCombo);
        finePanel.add(btnUpdate);
        content.add(finePanel);

        add(content, BorderLayout.CENTER);

        // Navigation back to Home
        btnBack.addActionListener(e -> mainFrame.showHome());

        // Interaction with Facade
        btnUpdate.addActionListener(e -> {
            String selected = (String) schemeCombo.getSelectedItem();
            facade.changeSystemFineScheme(selected);
            JOptionPane.showMessageDialog(this, "Fine System Updated to: " + selected);
        });
    }
}
