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
import javax.swing.table.DefaultTableModel;


public class AdminPanel extends JPanel {
    private MainFrame mainFrame;
    private ParkingSystemFacade facade;
    private JTabbedPane tabbedPane;
    private JLabel level1Count, level2Count, level3Count, level4Count, level5Count;
    private JLabel lblTotalAvailable;
    private JLabel lblOccupancyRate;

    public AdminPanel(ParkingSystemFacade facade, MainFrame mainFrame) {
        this.facade = facade;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());

        // 1. Top Navigation Bar
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(new Color(45, 45, 45));
        JButton btnBack = new JButton("Logout/Back");
        JLabel lblTitle = new JLabel("ADMINISTRATOR DASHBOARD", SwingConstants.CENTER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        navBar.add(btnBack, BorderLayout.WEST);
        navBar.add(lblTitle, BorderLayout.CENTER);
        add(navBar, BorderLayout.NORTH);

        // 2. Create Tabbed Pane
        tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Occupancy Monitoring", createOccupancyPanel());
        tabbedPane.addTab("Revenue Summary", createRevenuePanel());
        tabbedPane.addTab("Fine Overview", createFineOverviewPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Actions
        btnBack.addActionListener(e -> mainFrame.showHome());
    }

    // --- TAB 1: OCCUPANCY MONITORING ---
 private JPanel createOccupancyPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(new Color(240, 240, 240)); 
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Initialize labels for counts
    level1Count = createCountLabel();
    level2Count = createCountLabel();
    level3Count = createCountLabel();
    level4Count = createCountLabel();
    level5Count = createCountLabel();
    
    lblTotalAvailable = new JLabel("0", SwingConstants.CENTER);
    lblTotalAvailable.setOpaque(true);
    lblTotalAvailable.setBackground(new Color(144, 238, 144)); 
    lblTotalAvailable.setPreferredSize(new Dimension(80, 30));

    lblOccupancyRate = new JLabel("0.0%", SwingConstants.CENTER);
    lblOccupancyRate.setOpaque(true);
    lblOccupancyRate.setBackground(Color.LIGHT_GRAY);
    lblOccupancyRate.setPreferredSize(new Dimension(80, 30));
    
    // Adding Levels 1-5
    for (int i = 1; i <= 5; i++) {
        final int floorNum = i; 
        JButton btnLevel = new JButton("Level " + floorNum);
        
        // When clicked, show the details popup
        btnLevel.addActionListener(e -> showFloorDetails(floorNum));

        gbc.gridy = i;
        gbc.gridx = 0;
        panel.add(btnLevel, gbc);
        
        gbc.gridx = 1;
        if (i == 1) panel.add(level1Count, gbc);
        else if (i == 2) panel.add(level2Count, gbc);
        else if (i == 3) panel.add(level3Count, gbc);
        else if (i == 4) panel.add(level4Count, gbc);
        else if (i == 5) panel.add(level5Count, gbc);
    }
    
    // Total Available Section
    gbc.gridy = 6;
    gbc.gridx = 0;
    panel.add(new JLabel("Total Available Parking:"), gbc);
    gbc.gridx = 1;
    panel.add(lblTotalAvailable, gbc);
    
    // NEW: Occupancy Rate Section (GridY 7)
    gbc.gridy = 7;
    gbc.gridx = 0;
    panel.add(new JLabel("Occupancy Rate:"), gbc);
    gbc.gridx = 1;
    panel.add(lblOccupancyRate, gbc);
    
    
    gbc.gridy = 8;
    gbc.gridx = 0;
    gbc.gridwidth = 2; // Span across both columns
    JButton btnRefresh = new JButton("🔄 Refresh Data");
    btnRefresh.setBackground(new Color(70, 130, 180)); // Steel Blue
    btnRefresh.setForeground(Color.WHITE);
    btnRefresh.setFont(new Font("SansSerif", Font.BOLD, 12));
    
    // Action: Just call the update method
    btnRefresh.addActionListener(e -> {
        updateOccupancyDisplay();
        JOptionPane.showMessageDialog(this, "Occupancy Data Updated!");
    });
    
    panel.add(btnRefresh, gbc);
    
        
    updateOccupancyDisplay();
    return panel;
}

// Helper method to keep the gray boxes consistent
private JLabel createCountLabel() {
    JLabel label = new JLabel("0", SwingConstants.CENTER);
    label.setOpaque(true);
    label.setBackground(Color.LIGHT_GRAY);
    label.setPreferredSize(new Dimension(80, 30));
    label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    return label;
}

  private void showFloorDetails(int floor) {
    // This calls the facade to get: SpotID, Type, Status, LicensePlate, EntryTime
    List<Object[]> floorData = facade.getOccupancyDetailsByFloor(floor);

    String[] columns = {"Spot ID", "Spot Type", "Status", "Vehicle No", "Entry Time"};
    Object[][] data = new Object[floorData.size()][5];
    
    for (int i = 0; i < floorData.size(); i++) {
        data[i] = floorData.get(i);
    }

    JTable table = new JTable(data, columns);
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setPreferredSize(new Dimension(600, 400));

    JOptionPane.showMessageDialog(this, scrollPane, "Level " + floor + " Details", JOptionPane.PLAIN_MESSAGE);
}
  
  
   // --- TAB 2: REVENUE SUMMARY ---
private JPanel createRevenuePanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(new Color(245, 245, 245));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(15, 15, 15, 15);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // 1. General Revenue Button
    JButton btnGeneralReport = new JButton("📊 View General Revenue Report");
    btnGeneralReport.setFont(new Font("SansSerif", Font.BOLD, 13));
    btnGeneralReport.addActionListener(e -> showRevenueReport());

    // 2. Fine Analysis Button
    JButton btnFineReport = new JButton("💸 View Fine Strategy Analysis");
    btnFineReport.setFont(new Font("SansSerif", Font.BOLD, 13));
    btnFineReport.addActionListener(e -> showFineStrategyReport());

    gbc.gridy = 0;
    panel.add(btnGeneralReport, gbc);
    
    gbc.gridy = 1;
    panel.add(btnFineReport, gbc);

    return panel;
}

private void showFineStrategyReport() {
    // 1. Get Data from Facade
    List<Object[]> fineStats = facade.getFineRevenueReport();
    List<Object[]> topViolators = facade.getTopFineViolators();

    if (fineStats.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No fine records found yet.");
        return;
    }

    // 2. Setup the Strategy Breakdown Table
    String[] statCols = {"Fine Scheme", "Vehicles Fined", "Total Fine Revenue (RM)", "Avg. Fine (RM)"};
    DefaultTableModel statModel = new DefaultTableModel(statCols, 0);
    for (Object[] row : fineStats) statModel.addRow(row);
    JTable statTable = new JTable(statModel);

    // 3. Setup the Top Violators Table
    String[] violatorCols = {"Plate Number", "Scheme Used", "Total Duration", "Fine Amount (RM)"};
    DefaultTableModel violatorModel = new DefaultTableModel(violatorCols, 0);
    for (Object[] row : topViolators) violatorModel.addRow(row);
    JTable violatorTable = new JTable(violatorModel);

    // 4. NEW: Strategy Legend (Explaining Progressive vs Others)
    JPanel legendPanel = new JPanel(new GridLayout(3, 1));
    legendPanel.setBorder(BorderFactory.createTitledBorder("Strategy Logic Applied:"));
    legendPanel.add(new JLabel(" • Fixed: Flat RM 50.00"));
    legendPanel.add(new JLabel(" • Hourly: RM 10.00 per hour overstayed"));
    legendPanel.add(new JLabel(" • Progressive: RM 10 (1st hr) -> RM 20 (2nd hr) -> RM 40 (3rd hr+)"));

    // 5. Layout for the Popup
    JPanel container = new JPanel();
    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
    
    // Add sections with padding
    container.add(new JLabel("💰 REVENUE BY FINE STRATEGY"));
    container.add(new JScrollPane(statTable));
    container.add(Box.createRigidArea(new Dimension(0, 15)));
    
    container.add(new JLabel("🚩 TOP 5 HIGHEST FINES ISSUED"));
    container.add(new JScrollPane(violatorTable));
    container.add(Box.createRigidArea(new Dimension(0, 15)));
    
    container.add(legendPanel);

    container.setPreferredSize(new Dimension(650, 550));
    JOptionPane.showMessageDialog(this, container, "Fine Strategy Analytics", JOptionPane.PLAIN_MESSAGE);
}


    // --- TAB 4: FINE OVERVIEW & CONFIG ---
private JPanel createFineOverviewPanel() {
    JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
    mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // --- 1. TOP SECTION: Strategy Config & Rules ---
    JPanel topSection = new JPanel();
    topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));

    // Scheme Selection Row
    JPanel configRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
    configRow.setBorder(BorderFactory.createTitledBorder("Fine Rule Setup"));
    
    String[] schemes = {"Fixed", "Progressive", "Hourly"};
    JComboBox<String> schemeCombo = new JComboBox<>(schemes);
    schemeCombo.setSelectedItem(facade.getCurrentFineScheme());
    JButton btnUpdate = new JButton("Apply Scheme");
    JButton btnRefresh = new JButton("Refresh All Tables");

    configRow.add(new JLabel("Active Strategy: "));
    configRow.add(schemeCombo);
    configRow.add(btnUpdate);
    configRow.add(btnRefresh);

    // Rule Descriptions
    JTextArea txtRules = new JTextArea("• Fixed: RM 50 flat.\n• Progressive: Increases hourly (10->20->40).\n• Hourly: Based on overstay.");
    txtRules.setEditable(false);
    txtRules.setBackground(new Color(245, 245, 245));
    
    topSection.add(configRow);
    topSection.add(new JScrollPane(txtRules));
    mainContainer.add(topSection, BorderLayout.NORTH);

    // --- 2. CENTER SECTION: The Two Tables ---
    JPanel tablesPanel = new JPanel(new GridLayout(2, 1, 10, 10));

    // Table 1: Active Fines
    JPanel activePanel = new JPanel(new BorderLayout());
    activePanel.setBorder(BorderFactory.createTitledBorder("Active Fines (Currently Parked)"));
    String[] activeCols = {"Plate", "Type", "Spot", "Entry Time","Scheme", "Current Fine (RM)"};
    DefaultTableModel activeModel = new DefaultTableModel(activeCols, 0);
    activePanel.add(new JScrollPane(new JTable(activeModel)), BorderLayout.CENTER);

    // Table 2: Past Debt
    JPanel debtPanel = new JPanel(new BorderLayout());
    debtPanel.setBorder(BorderFactory.createTitledBorder("Unpaid Past Debt (Vehicle History)"));
    String[] debtCols = {"Plate", "Vehicle Type","Last Scheme", "Total Unpaid Debt (RM)"};
    DefaultTableModel debtModel = new DefaultTableModel(debtCols, 0);
    debtPanel.add(new JScrollPane(new JTable(debtModel)), BorderLayout.CENTER);

    tablesPanel.add(activePanel);
    tablesPanel.add(debtPanel);
    mainContainer.add(tablesPanel, BorderLayout.CENTER);

    // --- 3. Action Listeners ---
    btnUpdate.addActionListener(e -> {
        String selected = (String) schemeCombo.getSelectedItem();
        if(facade.changeSystemFineSchemeDb(selected)) {
            JOptionPane.showMessageDialog(this, "Rules updated to " + selected);
            // Refresh data here if needed
        }        
    });

    btnRefresh.addActionListener(e -> {
        // Method to call facade and update both activeModel and debtModel
        refreshBothTables(activeModel, debtModel);
    });

    return mainContainer;
}


public void updateOccupancyDisplay() {
    if (facade == null || level1Count == null) return;

    // 1. Fetch real-time available counts from the facade
    int f1 = facade.getAvailableSpotsByFloor(1);
    int f2 = facade.getAvailableSpotsByFloor(2);
    int f3 = facade.getAvailableSpotsByFloor(3);
    int f4 = facade.getAvailableSpotsByFloor(4);
    int f5 = facade.getAvailableSpotsByFloor(5);

    // 2. Update the Level UI labels (the gray boxes)
    level1Count.setText(String.valueOf(f1)); 
    level2Count.setText(String.valueOf(f2));
    level3Count.setText(String.valueOf(f3));
    level4Count.setText(String.valueOf(f4));
    level5Count.setText(String.valueOf(f5));

    // 3. Calculate Global Statistics
    int totalAvailable = f1 + f2 + f3 + f4 + f5;
    
    // FIX: Match this to your actual total spots (e.g., 250)
    int totalCapacity = 250; 
    
    int totalOccupied = totalCapacity - totalAvailable;

    lblTotalAvailable.setText(String.valueOf(totalAvailable));

    // Calculate Rate: (Occupied / Total) * 100
    double occupancyRate = (double) totalOccupied / totalCapacity;
    
    // This will now show 4.4% instead of -19.5%
    lblOccupancyRate.setText(String.format("%.1f%%", occupancyRate * 100));

    // Visual feedback logic
    if (occupancyRate > 0.9) {
        lblOccupancyRate.setBackground(new Color(255, 102, 102)); // Red
    } else {
        lblOccupancyRate.setBackground(new Color(144, 238, 144)); // Green
    }
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

        JLabel totalLabel = new JLabel(String.format("Total Revenue: RM %.2f", totalRevenue));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(totalLabel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel,
                "Revenue Report", JOptionPane.PLAIN_MESSAGE);
    }
     
    // Inside AdminPanel.java
private void refreshBothTables(DefaultTableModel activeModel, DefaultTableModel debtModel) {
    activeModel.setRowCount(0);
    debtModel.setRowCount(0);

    // Fills Active Fines (6 columns: Plate, Type, Spot, Time, Scheme, Fine)
    for (Object[] row : facade.getActiveFinesReport()) {
        activeModel.addRow(row);
    }

    // Fills Past Debt (4 columns: Plate, Type, Last Scheme, Total Debt)
    for (Object[] row : facade.getPastDebtReport()) {
        debtModel.addRow(row);
    }
}
     
     
   }
