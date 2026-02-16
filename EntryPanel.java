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
import java.util.ArrayList;
import java.util.List;

public class EntryPanel extends JPanel {

    private final MainFrame mainFrame;
    private final ParkingSystemFacade facade;

    private JTextField plateField;
    private JComboBox<String> typeCombo;
    private JCheckBox handicappedCheck;
    private JLabel lblDebtWarning;

    public EntryPanel(ParkingSystemFacade facade, MainFrame mainFrame) {
        this.facade = facade;
        this.mainFrame = mainFrame;

        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- UI ---
        JLabel title = new JLabel("Vehicle Entry System");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel lblPlate = new JLabel("License Plate:");
        plateField = new JTextField(15);

        JLabel lblType = new JLabel("Vehicle Type:");
        String[] types = {"Car", "Motorcycle", "SUV", "Handicapped"};
        typeCombo = new JComboBox<>(types);

        handicappedCheck = new JCheckBox("Handicapped driver (card holder)");
        handicappedCheck.setBackground(Color.WHITE);

        JButton btnReserve = new JButton("Reserve Parking Spot");
        JButton btnPark = new JButton("Assign Spot & Park");
        JButton btnBack = new JButton("Back to Main Menu");

        lblDebtWarning = new JLabel("");
        lblDebtWarning.setForeground(Color.RED);
        lblDebtWarning.setFont(new Font("SansSerif", Font.BOLD, 12));

        // --- Layout ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0;
        add(lblPlate, gbc);
        gbc.gridx = 1;
        add(plateField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        add(lblType, gbc);
        gbc.gridx = 1;
        add(typeCombo, gbc);

        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        add(handicappedCheck, gbc);

        // Buttons
        gbc.gridy = 4;
        add(btnReserve, gbc);

        gbc.gridy = 5;
        add(btnPark, gbc);

        gbc.gridy = 6;
        add(btnBack, gbc);

        gbc.gridy = 7;
        add(lblDebtWarning, gbc);

        // --- Actions ---
        btnBack.addActionListener(e -> mainFrame.showHome());
        btnReserve.addActionListener(e -> mainFrame.showReservation());

        btnPark.addActionListener(e -> {
            String plate = plateField.getText().trim().toUpperCase();
            String type = (String) typeCombo.getSelectedItem();
            boolean isCardHolder = handicappedCheck.isSelected();

            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter license plate.");
                return;
            }

            // --- Debt check ---
            double existingDebt = facade.checkExistingDebt(plate);
            if (existingDebt > 0) {
                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Vehicle has an outstanding fine of RM " + String.format("%.2f", existingDebt)
                                + ".\nContinue with entry?",
                        "Outstanding Debt Found",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (choice != JOptionPane.YES_OPTION) return;

                lblDebtWarning.setText("⚠️ UNPAID FINES: RM " + String.format("%.2f", existingDebt));
            } else {
                lblDebtWarning.setText("");
            }
            
            //FIRST: if this plate has an ACTIVE reservation, show ONLY reserved spot id
            List<String> reservedIds = facade.getReservedSpotsForPlate(plate); // DB-based (active only)

            if (reservedIds != null && !reservedIds.isEmpty()) {
                
                String chosenReservedId = (String) JOptionPane.showInputDialog(
                        this,
                        "You have an active reservation.\nSelect your reserved spot:",
                        "Reserved Spot",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        reservedIds.toArray(new String[0]),
                        reservedIds.get(0)
                );

                if (chosenReservedId == null) {
                    return;
                }                                
               
                String ticketResult = facade.handleVehicleEntry(plate, type, chosenReservedId, isCardHolder);

                if (ticketResult == null || ticketResult.startsWith("Error")) {
                    JOptionPane.showMessageDialog(this,
                            ticketResult == null ? "Unknown error." : ticketResult,
                            "Entry Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                plateField.setText("");
                handicappedCheck.setSelected(false);

                JTextArea textArea = new JTextArea(ticketResult);
                textArea.setEditable(false);
                JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Ticket Issued", JOptionPane.PLAIN_MESSAGE);

                mainFrame.showHome();
                return; //
            }


            // --- Get spot IDs from FACADE (this is the single source of truth) ---
            // 1) Get spot IDs from facade
            List<String> spotIds = facade.getAvailableSpotsFor(plate, type, isCardHolder);

            if (spotIds == null || spotIds.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No available spots.");
                return;
            }

            // 2) Convert IDs: ParkingSpot objects (so "(RESERVED)" shows)
            List<ParkingSpot> spotObjects = new ArrayList<>();
            for (String id : spotIds) {
                ParkingSpot ps = ParkingLot.getInstance().findSpotById(id);
                if (ps != null && ps.isAvailable()) {
                    spotObjects.add(ps);
                }
            }

            if (spotObjects.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No available spots.");
                return;
            }

            // 3) Show dropdown using ParkingSpot objects
            ParkingSpot selected = (ParkingSpot) JOptionPane.showInputDialog(
                    this,
                    "Select Available Spot:",
                    "Choose Spot",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    spotObjects.toArray(new ParkingSpot[0]),
                    spotObjects.get(0)
            );

            if (selected == null) {
                return;
            }

            // 4) Extract selected spot ID
            String selectedSpotId = selected.getSpotId();

            // --- Ticket issuance ---
            String ticketResult = facade.handleVehicleEntry(plate, type, selectedSpotId, isCardHolder);

            if (ticketResult == null || ticketResult.startsWith("Error")) {
                JOptionPane.showMessageDialog(
                        this,
                        ticketResult == null ? "Unknown error." : ticketResult,
                        "Entry Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Reset UI
            plateField.setText("");
            handicappedCheck.setSelected(false);

            JTextArea textArea = new JTextArea(ticketResult);
            textArea.setEditable(false);
            JOptionPane.showMessageDialog(
                    this,
                    new JScrollPane(textArea),
                    "Ticket Issued",
                    JOptionPane.PLAIN_MESSAGE
            );

            mainFrame.showHome();
        });
    }

    // Build the correct Vehicle object for ParkingLot filtering
    private Vehicle buildVehicle(String vehicleType, String plate, boolean isCardHolder) {
        // IMPORTANT:
        // This assumes your VehicleFactory supports createVehicle(vehicleType, plate)
        // and returns the correct Vehicle type.
        // If your VehicleFactory expects a different string (e.g., "SUV/Truck"), adjust here.
        Vehicle v = new VehicleFactory().createVehicle(vehicleType, plate);
        v.setHandicappedCardHolder(isCardHolder);
        return v;
    }

    // Convert DB reserved spot IDs into ParkingSpot objects (so the dialog shows "(Reserved)" etc.)
    private List<ParkingSpot> getReservedSpotsAsObjects(String plate) {
        List<String> reservedIds = facade.getReservedSpotsForPlate(plate); // DB-based (good)
        List<ParkingSpot> result = new ArrayList<>();

        if (reservedIds == null) return result;

        for (String id : reservedIds) {
            ParkingSpot ps = ParkingLot.getInstance().findSpotById(id);
            // Only show if exists AND currently available in memory
            if (ps != null && ps.isAvailable()) {
                result.add(ps);
            }
        }
        return result;
    }
}
