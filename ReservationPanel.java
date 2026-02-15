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

public class ReservationPanel extends JPanel {   
    private ParkingSystemFacade facade;
    private MainFrame mainFrame;

    public ReservationPanel(ParkingSystemFacade facade, MainFrame mainFrame) {
        this.facade = facade;
        this.mainFrame = mainFrame;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Reserve Parking Spot");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JTextField plateField = new JTextField(15);
        JTextField spotField  = new JTextField(15);

        SpinnerNumberModel hoursModel = new SpinnerNumberModel(2, 1, 24, 1);
        JSpinner hoursSpinner = new JSpinner(hoursModel);

        JLabel msg = new JLabel(" ");

        JButton btnCreate = new JButton("Create Reservation");
        JButton btnBack = new JButton("Back");

        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; add(title, gbc);
        gbc.gridwidth=1;

        gbc.gridx=0; gbc.gridy=1; add(new JLabel("Plate:"), gbc);
        gbc.gridx=1; add(plateField, gbc);

        gbc.gridx=0; gbc.gridy=2; add(new JLabel("Reserved Spot ID:"), gbc);
        gbc.gridx=1; add(spotField, gbc);

        gbc.gridx=0; gbc.gridy=3; add(new JLabel("Duration (hours):"), gbc);
        gbc.gridx=1; add(hoursSpinner, gbc);

        gbc.gridx=0; gbc.gridy=4; add(btnCreate, gbc);
        gbc.gridx=1; add(btnBack, gbc);

        gbc.gridx=0; gbc.gridy=5; gbc.gridwidth=2; add(msg, gbc);
        
        btnBack.addActionListener(e -> mainFrame.showHome());

        btnCreate.addActionListener(e -> {
            String plate = plateField.getText().trim();
            String spotId = spotField.getText().trim();

            if (plate.isEmpty() || spotId.isEmpty()) {
                msg.setText("Plate and Spot ID are required.");
                return;
            }

            // Finding spot that is a ReservedSpot
            ParkingSpot spot = ParkingLot.getInstance().findSpotById(spotId);
            if (!(spot instanceof ReservedSpot)) {
                msg.setText("Error: Spot is not a RESERVED spot.");
                return;
            }

            int hours = (Integer) hoursSpinner.getValue();
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.plusHours(hours);

            String reservationId = "R-" + plate + "-" + System.currentTimeMillis();

            Reservation r = new Reservation(
                    reservationId,
                    plate,
                    (ReservedSpot) spot,
                    start,
                    end,
                    ReservationStatus.ACTIVE
            );
            
            ParkingRepository repo = new ParkingRepository();
            boolean saved = repo.createReservation(r);

            if (saved) {
                msg.setText("Reservation created! Plate " + plate + " can use " + spotId);
            } else {
                msg.setText("Failed to save reservation into database.");
            }

            ParkingLot.getInstance().addReservation(r);
            msg.setText("Reservation created! Plate " + plate + " can use " + spotId);
        });
    }
}
