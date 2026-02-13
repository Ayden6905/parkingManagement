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
import java.time.format.DateTimeFormatter;

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
        String sqlVehicle = "INSERT INTO vehicle (licensePlate, vehicleType) VALUES (?, ?)";
        String sqlTicket = "INSERT INTO ticket (ticketId, licensePlate, spotId, entryTime) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            try (PreparedStatement psCheck = conn.prepareStatement(
                "SELECT licensePlate FROM vehicle WHERE licensePlate = ?")) {

            psCheck.setString(1, this.licensePlate);
            ResultSet rs = psCheck.executeQuery();

            if (!rs.next()) {
                try (PreparedStatement psV = conn.prepareStatement(sqlVehicle)) {
                    psV.setString(1, this.licensePlate);
                    psV.setString(2, "Car"); 
                    psV.executeUpdate();
                }
            }
        }
        
        String updateSpot = "UPDATE parkingSpot SET status='Occupied' WHERE spotId=?";

            try (PreparedStatement psUpdate = conn.prepareStatement(updateSpot)) {
                psUpdate.setString(1, spotId);
                psUpdate.executeUpdate();
        }

            try (PreparedStatement psT = conn.prepareStatement(sqlTicket)) {
                psT.setString(1, ticketId);
                psT.setString(2, licensePlate);
                psT.setString(3, spotId);
                psT.setTimestamp(4, Timestamp.valueOf(entryTime));
                psT.executeUpdate();
                System.out.println("Success: Ticket saved to database.");
            }
        }
        catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage()); 
            e.printStackTrace();
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
    
    public String generateFormattedTicket(String vehicleType) {
        java.time.format.DateTimeFormatter timeFmt = java.time.format.DateTimeFormatter.ofPattern("h:mm a");
        
        long timestamp = System.currentTimeMillis();
        String headerId = "T-" + this.licensePlate + "-" + timestamp;
        
        String level = "L3"; //dummy for now
        
        return "==========================================\n" +
               "          " + headerId + "\n" +
               "     ex: " + vehicleType.toLowerCase() + "-" + this.licensePlate + "-1410\n" +
               "==========================================\n\n" +
               "   Entry Time:     " + this.entryTime.format(timeFmt) + "\n" +
               "   Vehicle Type:   " + vehicleType + "\n" +
               "   Plate Number:   " + this.licensePlate + "\n" +
               "   Assigned Level: " + level + "\n" +
               "   Assigned Spot:  " + this.spotId + "\n\n" +
               "==========================================";
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
