/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author User
 */
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;

public class Ticket {
    private String ticketId;
    private Vehicle licensePlate;
    private ParkingSpot spotId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private int totalHours;
    private double parkingFee;
    private double fineAmount;
    private double totalPaid;
    private String paymentMethod;
   
    
    public Ticket(String ticketId, Vehicle licensePlate, ParkingSpot spotId, 
            LocalDateTime entryTime, LocalDateTime exitTime,
            double parkingFee, double fineAmount, double totalPaid,
            String paymentMethod) {
        this.ticketId = ticketId;
        this.licensePlate = licensePlate;
        this.spotId = spotId;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.parkingFee = parkingFee;
        this.fineAmount = fineAmount;
        this.totalPaid = totalPaid;
        this.paymentMethod = paymentMethod;
        this.totalHours = calculateDurationHours();
    }
    
    public String generateTicketId() {
        return "T-" + licensePlate.getLicensePlate() + "-" + System.currentTimeMillis();
    }
    
    public int calculateDurationHours() {
        LocalDateTime end = (exitTime != null) ? exitTime : LocalDateTime.now();
        long minutes = Duration.between(entryTime, end).toMinutes();
        totalHours = (int) Math.ceil(minutes / 60.0);
        return totalHours;
    }
    
    public void saveEntry() {
        String sqlVehicle = "INSERT IGNORE INTO vehicle (licensePlate, vehicleType) VALUES (?, ?)";
        String sqlTicket = "INSERT INTO ticket (ticketId, licensePlate, spotId, entryTime) VALUES (?, ?, ?, ?)";
        String updateSpot = "UPDATE parkingSpot SET status='Occupied' WHERE spotId=?";

        try (Connection conn = DatabaseConfig.getConnection()) {

            try (PreparedStatement psVehicle = conn.prepareStatement(sqlVehicle)) {
                psVehicle.setString(1, licensePlate.getLicensePlate());
                psVehicle.setString(2, licensePlate.getClass().getSimpleName()); 
                psVehicle.executeUpdate();
            }

            try (PreparedStatement psSpot = conn.prepareStatement(updateSpot)) {
                psSpot.setString(1, spotId.getSpotId());
                psSpot.executeUpdate();
            }

            try (PreparedStatement psTicket = conn.prepareStatement(sqlTicket)) {
                psTicket.setString(1, ticketId);
                psTicket.setString(2, licensePlate.getLicensePlate());
                psTicket.setString(3, spotId.getSpotId());
                psTicket.setTimestamp(4, Timestamp.valueOf(entryTime));
                psTicket.executeUpdate();
            }

            System.out.println("Ticket entry saved successfully.");

        } catch (SQLException e) {
            System.out.println("DB Error (saveEntry): " + e.getMessage());
        }
    }
    
    public void closeTicket(LocalDateTime exitTime, double parkingFee,
                            double fineAmount, double totalPaid, String paymentMethod) {
        this.exitTime = exitTime;
        this.parkingFee = parkingFee;
        this.fineAmount = fineAmount;
        this.totalPaid = totalPaid;
        this.paymentMethod = paymentMethod;
        calculateDurationHours();

        String sql = "UPDATE ticket SET exitTime=?, parkingFee=?, fineAmount=?, totalPaid=?, paymentMethod=? WHERE ticketId=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(exitTime));
            ps.setDouble(2, parkingFee);
            ps.setDouble(3, fineAmount);
            ps.setDouble(4, totalPaid);
            ps.setString(5, paymentMethod);
            ps.setString(6, ticketId);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("DB Error (closeTicket): " + e.getMessage());
        }
    }
    
    public String getTicketId() { return ticketId; }
    public Vehicle getLicensePlate() { return licensePlate; }
    public ParkingSpot getSpotId() { return spotId; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public int getTotalHours() { return totalHours; }
    public double getParkingFee() { return parkingFee; }
    public double getFineAmount() { return fineAmount; }
    public double getTotalPaid() { return totalPaid; }
    public String getPaymentMethod() { return paymentMethod; }
}
