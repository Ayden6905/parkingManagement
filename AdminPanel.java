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
import java.util.List;

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
        
        JPanel revenuePanel = new JPanel(new FlowLayout());
        revenuePanel.setBorder(BorderFactory.createTitledBorder("Revenue Report"));
        
        JButton btnRevenue = new JButton("View Revenue Report");
        revenuePanel.add(btnRevenue);
        
        content.add(revenuePanel);

        add(content, BorderLayout.CENTER);

        // Navigation back to Home
        btnBack.addActionListener(e -> mainFrame.showHome());

        // Interaction with Facade
        btnUpdate.addActionListener(e -> {
            String selected = (String) schemeCombo.getSelectedItem();
            facade.changeSystemFineScheme(selected);
            JOptionPane.showMessageDialog(this, "Fine System Updated to: " + selected);
        });
        
        btnRevenue.addActionListener(e -> showRevenueReport());
    }
    
    private void showRevenueReport() {
        List<RevenueRecord> records = facade.getRevenueReport();

        if (records.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No revenue records found.");
            return;
        }

        String[] columns = {
                "License Plate",
                "Entry Time",
                "Exit Time",
                "Total Paid (RM)",
                "Payment Method",
                "Receipt Time"
        };

        Object[][] data = new Object[records.size()][6];

        double totalRevenue = 0;

        for (int i = 0; i < records.size(); i++) {
            RevenueRecord r = records.get(i);

            data[i][0] = r.getLicensePlate();
            data[i][1] = r.getEntryTime();
            data[i][2] = r.getExitTime();
            data[i][3] = r.getTotalPaid();
            data[i][4] = r.getPaymentMethod();
            data[i][5] = r.getReceiptTime();

            totalRevenue += r.getTotalPaid();
        }

        JTable table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel totalLabel = new JLabel("Total Revenue: RM " + totalRevenue);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(totalLabel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel,
                "Revenue Report", JOptionPane.PLAIN_MESSAGE);
    }
}
