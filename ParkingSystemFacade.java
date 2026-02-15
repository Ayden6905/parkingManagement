/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parkingmanagement;
/**
 *
 * @author NurqistinaAtashah
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class ParkingSystemFacade {

    private final FineManager fineManager;
    private final TicketService ticketService;

    public ParkingSystemFacade() {
        this.fineManager = new FineManager();
        this.ticketService = new TicketService();

        String savedScheme = getCurrentFineScheme();
        this.fineManager.setStrategy(savedScheme);
        System.out.println("System loaded with Fine Scheme: " + savedScheme);
    }

    //db check
    public boolean checkDatabaseConnection() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //admin login
    public boolean authenticateAdmin(String username, String password) {

        String query = "SELECT * FROM admin WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.out.println("Login Error: " + e.getMessage());
            return false;
        }
    }

  public String handleVehicleEntry(String plate, String vehicleType, String spotId) {
    // 1. Basic Validation
    if (plate == null || plate.trim().isEmpty()) {
        return "Error: License plate required.";
    }

    // 2. Business Rule: Prevent duplicate entries for the same car
    if (Ticket.findActiveByPlate(plate) != null) {
        return "Error: Vehicle with plate " + plate + " is already inside the parking lot.";
    }

    try {
        // 3. Capture the SNAPSHOT of the current system fine scheme
        // This is what ensures the "Contract" logic works later at exit
        String activeScheme = getCurrentFineScheme(); 

        // 4. Request TicketService to create the record
        // Note: We now pass 4 arguments (plate, type, spot, and the scheme)
        ticketService.createTicket(plate, vehicleType, spotId, activeScheme);

        // 5. Retrieve the newly created ticket to show the user
        Ticket ticket = Ticket.findActiveByPlate(plate);

        if (ticket != null) {
            return ticket.generateFormattedTicket();
        }

    } catch (Exception e) {
        System.out.println("Entry Error: " + e.getMessage());
        return "Error during vehicle entry: " + e.getMessage();
    }

    return "Error: Failed to generate ticket.";
}  
    
    
    
    //vehicle entry
   public Receipt handleVehicleExit(String plate) {
    if (plate == null || plate.trim().isEmpty()) return null;

    // 1. Fetch the ticket (this retrieves the 'fineScheme' from the DB)
    Ticket ticket = Ticket.findActiveByPlate(plate);
    if (ticket == null) return null;

    // 2. FORCE the FineManager to use this specific car's entry scheme
    // This is the "contract" part—it ignores the current global setting
    String schemeUsedAtEntry = ticket.getFineScheme();
    fineManager.setStrategy(schemeUsedAtEntry);

    // 3. Perform calculations
    int hoursParked = ticket.calculateDurationHours();
    double hourlyRate = 3.00;
    
    // This calculation now uses the strategy we set in step 2
    double fine = fineManager.calculateFine(hoursParked);

    // 4. Finalize the payment
    return ticketService.closeTicketAndPay(plate, hourlyRate, fine, "Cash");
}
    
   
    //to change fine scheme
    public boolean changeSystemFineSchemeDb(String schemeName) {

        String sql = "UPDATE fineStrategy SET current_scheme = ? WHERE id = 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, schemeName);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                fineManager.setStrategy(schemeName);
                System.out.println("Strategy switched to: " + schemeName);
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error updating fine scheme: " + e.getMessage());
        }

        return false;
    }

    public String getCurrentFineScheme() {

        String sql = "SELECT current_scheme FROM fineStrategy WHERE id = 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getString("current_scheme");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Fixed"; // default
    }

    //parking summary
    public ParkingSummary getParkingSummary(String plate, double hourlyRate) {
    Ticket ticket = Ticket.findActiveByPlate(plate);
    if (ticket == null) return null;

    // Apply the historical scheme for the preview
    fineManager.setStrategy(ticket.getFineScheme());

    int duration = ticket.calculateDurationHours();
    double fineOwed = fineManager.calculateFine(duration);
    double parkingFee = duration * hourlyRate;

    return new ParkingSummary(
            ticket.getTicketId(),
            ticket.getLicensePlate().getLicensePlate(),
            ticket.getEntryTime(),
            LocalDateTime.now(),
            duration,
            parkingFee,
            fineOwed,
            parkingFee + fineOwed
    );
}

    //available spots
    public List<String> getAvailableSpots() {

        List<String> spots = new java.util.ArrayList<>();

        String sql = "SELECT spotId FROM parkingSpot WHERE status='Available'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                spots.add(rs.getString("spotId"));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching spots: " + e.getMessage());
        }

        return spots;
    }

    //payment processing
    public Receipt processPayment(String plate,
                                  double hourlyRate,
                                  double fineToPay,
                                  String method) {

        return ticketService.closeTicketAndPay(
                plate,
                hourlyRate,
                fineToPay,
                method
        );
    }

    //revenue report
    public List<RevenueRecord> getRevenueReport() {
        return ticketService.getRevenueReport();
    }
    
 
    
    public int getAvailableSpotsByFloor(int floor) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM parkingSpot WHERE floorNumber = ? AND status = 'Available'";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, floor);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
    
    public List<Object[]> getOccupancyDetailsByFloor(int floor) {
        List<Object[]> details = new ArrayList<>(); // Now compiles with import above
        String sql = "SELECT p.spotId, p.spotType, p.status, t.licensePlate, t.entryTime " +
                     "FROM parkingSpot p " +
                     "LEFT JOIN ticket t ON p.spotId = t.spotId AND t.exitTime IS NULL " +
                     "WHERE p.floorNumber = ? " +
                     "ORDER BY p.spotId ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, floor);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                details.add(new Object[]{
                    rs.getString("spotId"),
                    rs.getString("spotType"),
                    rs.getString("status"),
                    rs.getString("licensePlate") == null ? "-" : rs.getString("licensePlate"),
                    rs.getTimestamp("entryTime") == null ? "-" : rs.getTimestamp("entryTime").toString()
                });
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return details;
    }
    
    public List<Object[]> getVehiclesWithFines() {
    List<Object[]> list = new ArrayList<>();
    
    // 1. We query the DB for all vehicles currently inside (exitTime IS NULL)
    String sql = "SELECT licensePlate FROM ticket WHERE exitTime IS NULL";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            // 2. For every plate found, fetch the full Ticket object
            // This object now contains the 'fineScheme' saved at entry
            Ticket t = Ticket.findActiveByPlate(rs.getString("licensePlate"));
            
            if (t != null) {
                // 3. CRITICAL STEP: Set the manager to THIS ticket's scheme
                // This prevents Car A (Hourly) from being calculated as Fixed
                fineManager.setStrategy(t.getFineScheme());
                
                int hours = t.calculateDurationHours();
                double fineAmount = fineManager.calculateFine(hours);

                // 4. Only add to list if they actually owe money
                if (fineAmount > 0) {
                    list.add(new Object[]{
                        t.getLicensePlate().getLicensePlate(),
                        t.getEntryTime().toString(),
                        hours + " hrs",
                        String.format("%.2f", fineAmount),
                        "UNPAID (" + t.getFineScheme() + ")" // Displays the scheme applied
                    });
                }
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}
    
    // --- FINE ANALYTICS REPORT ---
public List<Object[]> getFineRevenueReport() {
    List<Object[]> report = new ArrayList<>();
    // This query calculates how much money was made from fines under each specific rule
    String sql = "SELECT fineScheme, COUNT(*) as totalFinedVehicles, " +
                 "SUM(fineAmount) as totalFineRevenue, AVG(fineAmount) as averageFine " +
                 "FROM ticket WHERE exitTime IS NOT NULL AND fineAmount > 0 " +
                 "GROUP BY fineScheme";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            report.add(new Object[]{
                rs.getString("fineScheme"),              // "Fixed" or "Hourly"
                rs.getInt("totalFinedVehicles"),         // Number of cars caught
                String.format("%.2f", rs.getDouble("totalFineRevenue")), // Total money
                String.format("%.2f", rs.getDouble("averageFine"))       // Average per car
            });
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return report;
}

// --- TOP 5 HIGHEST FINES (The "Violators" List) ---
public List<Object[]> getTopFineViolators() {
    List<Object[]> violators = new ArrayList<>();
    String sql = "SELECT licensePlate, fineScheme, totalHours, fineAmount " +
                 "FROM ticket WHERE fineAmount > 0 " +
                 "ORDER BY fineAmount DESC LIMIT 5";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            violators.add(new Object[]{
                rs.getString("licensePlate"),
                rs.getString("fineScheme"),
                rs.getInt("totalHours") + " hrs",
                String.format("%.2f", rs.getDouble("fineAmount"))
            });
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return violators;
}
}
