/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author User
 */
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;

public class Ticket {
    private String ticketId;
    private String licensePlate;
    private String spotId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private double parkingFee;
    private double fineAmount;
    private double totalPaid;
    private String paymentMethod;
    
    public Ticket(String ticketId, String licensePlate, String spotId, LocalDateTime entryTime) {
        this.ticketId = ticketId;
        this.licensePlate = licensePlate;
        this.spotId = spotId;
        this.entryTime = entryTime;
    }
    
    public Ticket(String ticketId, String licensePlate, String spotId,
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
    }
    
    public int calculateDurationHours() {
        LocalDateTime end = (exitTime != null) ? exitTime : LocalDateTime.now();
        long minutes = Duration.between(entryTime, end).toMinutes();
        return (int) Math.ceil(minutes / 60.0); //for ceiling rounding
    }
    
    //db operation
    public void saveEntry() {
        String sql = "INSERT INTO ticket (ticketId, licensePlate, spotId, entryTime) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, ticketId);
            ps.setString(2, licensePlate);
            ps.setString(3, spotId);
            ps.setTimestamp(4, Timestamp.valueOf(entryTime));
            ps.executeUpdate();
        }
        
        catch (SQLException e) {
            System.out.println("Error saving ticket entry: " + e.getMessage());       
        }
    }
    
    public void closeTicket(LocalDateTime exitTime, double parkingFee,
            double fineAmount, double totalPaid, String paymentMethod) {
        
        this.exitTime = exitTime;
        this.parkingFee = parkingFee;
        this.fineAmount = fineAmount;
        this.totalPaid = totalPaid;
        this.paymentMethod = paymentMethod;
        
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
        }
        
        catch (SQLException e) {
            System.out.println("Error closing ticket: " + e.getMessage());
        }   
    }
    
    public static Ticket findActiveByPlate(String plate) {
        String sql = "SELECT * FROM ticket WHERE licensePlate=? AND exitTime IS NULL";
        
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Ticket(
                        rs.getString("ticketId"),
                        rs.getString("licensePlate"),
                        rs.getString("spotId"),
                        rs.getTimestamp("entryTime").toLocalDateTime()
                );
            }

        } catch (SQLException e) {
            System.out.println("Error finding active ticket: " + e.getMessage());
        }
        return null;
    }
    
    //getters
    public String getTicketId() { return ticketId; }
    public String getLicensePlate() { return licensePlate; }
    public String getSpotId() { return spotId; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public double getParkingFee() { return parkingFee; }
    public double getFineAmount() { return fineAmount; }
    public double getTotalPaid() { return totalPaid; }
    public String getPaymentMethod() { return paymentMethod; }
}
