/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel to handle user reservations for specific parking spots.
 */
public class ReservationPanel extends JPanel {   
    private ParkingSystemFacade facade;
    private MainFrame mainFrame;
    private JComboBox<String> spotDropdown;
    private JTextField plateField; // Declared at class level
    private JLabel msg;
    
    public ReservationPanel(ParkingSystemFacade facade, MainFrame mainFrame) {
        this.facade = facade;
        this.mainFrame = mainFrame;

        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Title ---
        JLabel title = new JLabel("Reserve Parking Spot");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        // --- Input Fields ---
        plateField = new JTextField(15);
        
        spotDropdown = new JComboBox<>();
        refreshAvailableReservedSpots(); // Initial load

        SpinnerNumberModel hoursModel = new SpinnerNumberModel(2, 1, 24, 1);
        JSpinner hoursSpinner = new JSpinner(hoursModel);

        msg = new JLabel(" "); // Space keeps layout consistent

        // --- Buttons ---
        JButton btnCreate = new JButton("Create Reservation");
        JButton btnBack = new JButton("Back");

        // --- UI Layout ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; 
        add(title, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; add(new JLabel("Plate:"), gbc);
        gbc.gridx = 1; add(plateField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; add(new JLabel("Available Reserved Spots:"), gbc);
        gbc.gridx = 1; add(spotDropdown, gbc);

        gbc.gridx = 0; gbc.gridy = 3; add(new JLabel("Duration (hours):"), gbc);
        gbc.gridx = 1; add(hoursSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 4; add(btnCreate, gbc);
        gbc.gridx = 1; add(btnBack, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; 
        add(msg, gbc);
        
        // --- Listeners ---
        btnBack.addActionListener(e -> mainFrame.showHome());

        btnCreate.addActionListener(e -> {
            String plate = plateField.getText().trim();
            String spotId = (String) spotDropdown.getSelectedItem();

            // Validation
            if (plate.isEmpty() || spotId == null || spotId.equals("No Reserved Spots Available")) {
                msg.setText("Plate and Spot Selection are required.");
                msg.setForeground(Color.RED);
                return;
            }

            ParkingSpot spot = ParkingLot.getInstance().findSpotById(spotId);
            
            int hours = (Integer) hoursSpinner.getValue();
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.plusHours(hours);

            String reservationId = "R-" + plate + "-" + System.currentTimeMillis();

            // Create object
            Reservation r = new Reservation(
                    reservationId, plate, (ReservedSpot) spot, start, end, ReservationStatus.ACTIVE
            );
            
            ParkingRepository repo = new ParkingRepository();
            if (repo.createReservation(r)) {
                // 1. Sync Memory: Mark spot as OCCUPIED
                spot.setStatus(SpotStatus.OCCUPIED); 
                ParkingLot.getInstance().addReservation(r);
                
                // 2. Feedback to User
                msg.setText("Success! Spot " + spotId + " reserved for " + plate);
                msg.setForeground(new Color(0, 153, 0)); // Success Green
                
                // 3. Reset UI for next use
                plateField.setText("");
                refreshAvailableReservedSpots(); 
            } else {
                msg.setText("Database Error: Could not save reservation.");
                msg.setForeground(Color.RED);
            }
        });
    }

    /**
     * Updates the JComboBox with spots that are of type ReservedSpot and are currently Available.
     */
    public void refreshAvailableReservedSpots() {
        spotDropdown.removeAllItems();
        
        List<String> availableReserved = ParkingLot.getInstance().getSpots().values().stream()
            .filter(s -> s instanceof ReservedSpot)
            .filter(s -> s.getStatus() == SpotStatus.AVAILABLE)
            .map(ParkingSpot::getSpotId)
            .collect(Collectors.toList());

        if (availableReserved.isEmpty()) {
            spotDropdown.addItem("No Reserved Spots Available");
            spotDropdown.setEnabled(false);
        } else {
            spotDropdown.setEnabled(true);
            availableReserved.forEach(spotDropdown::addItem);
        }
    }
}