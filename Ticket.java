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
    private String fineScheme;
    private double carriedOverFine;
   
    
    public Ticket(String ticketId, Vehicle licensePlate, ParkingSpot spotId, 
            LocalDateTime entryTime, String  fineScheme, double carriedOverFine) {
        this.ticketId = ticketId;
        this.licensePlate = licensePlate;
        this.spotId = spotId;
        this.entryTime = entryTime;
        this.fineScheme = fineScheme;
        this.carriedOverFine = carriedOverFine;
        
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
    
    public long calculateDuration() {
    return (long) calculateDurationHours();
}
    
    public void saveEntry() {
    String sqlVehicle = "INSERT IGNORE INTO vehicle (licensePlate, vehicleType) VALUES (?, ?)";
    // 1. ADDED carriedOverFine to the column list and a 6th '?' placeholder
    String sqlTicket = "INSERT INTO ticket (ticketId, licensePlate, spotId, entryTime, fineScheme, carriedOverFine) VALUES (?, ?, ?, ?, ?, ?)";
    String updateSpot = "UPDATE parkingSpot SET status='Occupied' WHERE spotId=?";

    try (Connection conn = DatabaseConfig.getConnection()) {

        // Save Vehicle info (if not exists)
        try (PreparedStatement psVehicle = conn.prepareStatement(sqlVehicle)) {
            psVehicle.setString(1, licensePlate.getLicensePlate());
            psVehicle.setString(2, licensePlate.getClass().getSimpleName()); 
            psVehicle.executeUpdate();
        }

        // Update Spot status
        try (PreparedStatement psSpot = conn.prepareStatement(updateSpot)) {
            psSpot.setString(1, spotId.getSpotId());
            psSpot.executeUpdate();
        }

        // Save Ticket info
        try (PreparedStatement psTicket = conn.prepareStatement(sqlTicket)) {
            psTicket.setString(1, ticketId);
            psTicket.setString(2, licensePlate.getLicensePlate());
            psTicket.setString(3, spotId.getSpotId());
            psTicket.setTimestamp(4, Timestamp.valueOf(entryTime));
            psTicket.setString(5, fineScheme);
            
            // 2. ADDED this line to save the debt into the ticket record
            psTicket.setDouble(6, this.carriedOverFine); 
            
            psTicket.executeUpdate();
        }

        System.out.println("Ticket entry saved successfully with carried over fine: RM " + carriedOverFine);

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
    
    public ParkingSpot getSpot() {
    return this.spotId; // Changed from .spot to .spotId
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
    
    // --- UPDATED FIND ACTIVE TICKET ---
   public static Ticket findActiveByPlate(String plate) {
    // 1. Added vehicleType to the SELECT so we can tell the Factory what to create
    String sql = "SELECT t.ticketId, t.licensePlate, t.spotId, t.entryTime, t.fineScheme, t.carriedOverFine, v.vehicleType "
               + "FROM ticket t "
               + "JOIN vehicle v ON t.licensePlate = v.licensePlate "
               + "WHERE t.licensePlate=? AND t.exitTime IS NULL";

    try (Connection conn = DatabaseConfig.getConnection(); 
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, plate);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                // 2. Fetch the type from the DB result
                String typeStr = rs.getString("vehicleType");
                double debt = rs.getDouble("carriedOverFine");

                // 3. Create the vehicle with the correct type and fine
                Vehicle v = SimpleVehicleFactory.createVehicle(plate, typeStr, debt);
                
                // You may need to adjust this depending on how you store Spot details
                ParkingSpot s = new RegularSpot(rs.getString("spotId"), 1); 

                return new Ticket(
                        rs.getString("ticketId"),
                        v,
                        s,
                        rs.getTimestamp("entryTime").toLocalDateTime(),
                        rs.getString("fineScheme"),
                        debt
                ); 
            }
        }
    } catch (SQLException e) {
        System.out.println("Error finding active ticket: " + e.getMessage());
    }
    return null;
}
    
    public double getCarriedOverFine() {
        return carriedOverFine;
    }

    // --- GETTER FOR SCHEME ---
    public String getFineScheme() { return fineScheme; }
    
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

