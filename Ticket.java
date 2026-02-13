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
            LocalDateTime entryTime) {
        this.ticketId = ticketId;
        this.licensePlate = licensePlate;
        this.spotId = spotId;
        this.entryTime = entryTime;
        
        //default value used for entry ticket
        this.exitTime = null;
        this.parkingFee = 0.0;
        this.fineAmount = 0.0;
        this.totalPaid = 0.0;
        this.paymentMethod = "N/A";
        this.totalHours = 0;
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
    
    public String generateFormattedTicket() {
        java.time.format.DateTimeFormatter timeFmt = java.time.format.DateTimeFormatter.ofPattern("h:mm a");

        String plateStr = (licensePlate != null) ? licensePlate.getLicensePlate() : "N/A";
        String spotStr = (spotId != null) ? spotId.getSpotId() : "N/A";
        String level = (spotStr.contains("-")) ? spotStr.split("-")[0] : "N/A"; // F1 from F1-R1-S1

        return "==========================================\n"
                + "          " + ticketId + "\n"
                + "==========================================\n\n"
                + "   Entry Time:     " + entryTime.format(timeFmt) + "\n"
                + "   Plate Number:   " + plateStr + "\n"
                + "   Assigned Level: " + level + "\n"
                + "   Assigned Spot:  " + spotStr + "\n\n"
                + "==========================================";
    }
    
    public static Ticket findActiveByPlate(String plate)
    {
        String sql = "SELECT ticketId, licensePlate, spotId, entryTime "
                + "FROM ticket WHERE licensePlate=? AND exitTime IS NULL";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, plate);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    // stub for now
                    Vehicle v = new Car(rs.getString("licensePlate")); // or your Vehicle base constructor
                    ParkingSpot s = new RegularSpot(rs.getString("spotId"), 1); // placeholder floor

                    return new Ticket(
                            rs.getString("ticketId"),
                            v,
                            s,
                            rs.getTimestamp("entryTime").toLocalDateTime()
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Error finding active ticket: " + e.getMessage());
        }

        return null;
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
