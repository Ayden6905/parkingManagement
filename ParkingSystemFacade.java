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
    if (plate == null || plate.trim().isEmpty()) {
        return "Error: License plate required.";
    }

    if (Ticket.findActiveByPlate(plate) != null) {
        return "Error: Vehicle with plate " + plate + " is already inside.";
    }

    try {
        // 1. THIS IS THE LINE: Check the database for old fines linked to this plate
        double existingDebt = fineManager.getOutstandingFineByPlate(plate);

        // 2. Get the active strategy (Fixed/Hourly etc.)
        String activeScheme = getCurrentFineScheme(); 

        // 3. PASS existingDebt HERE: This ensures the NEW ticket knows about the OLD debt
        ticketService.createTicket(plate, vehicleType, spotId, activeScheme, existingDebt);

        Ticket ticket = Ticket.findActiveByPlate(plate);
        if (ticket != null) {
            String receipt = ticket.generateFormattedTicket();
            
            // 4. Visual confirmation for the user
            if (existingDebt > 0) {
                receipt += "\n⚠️ UNPAID FINES DETECTED: RM " + String.format("%.2f", existingDebt);
            }
            return receipt;
        }

    } catch (Exception e) {
        return "Error during entry: " + e.getMessage();
    }
    return "Error: Failed to generate ticket.";
}

public double checkExistingDebt(String plate) {
    // This calls your fineManager to get the number from the SQL table
    return fineManager.getOutstandingFineByPlate(plate);
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
    
    // We JOIN ticket (current stay) and vehicle (permanent record of debt)
    String sql = "SELECT t.licensePlate, t.entryTime, t.fineScheme, v.outstandingFines " +
                 "FROM ticket t " +
                 "JOIN vehicle v ON t.licensePlate = v.licensePlate " +
                 "WHERE t.exitTime IS NULL";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            String plate = rs.getString("licensePlate");
            String scheme = rs.getString("fineScheme");
            double pastDebt = rs.getDouble("outstandingFines"); // <--- This pulls the "Not Paid" debt

            // Calculate current stay fine
            Ticket t = Ticket.findActiveByPlate(plate);
            int hours = t.calculateDurationHours();
            fineManager.setStrategy(scheme);
            double currentFine = fineManager.calculateFine(hours);

            // Add to table if they owe ANYTHING
            if (currentFine > 0 || pastDebt > 0) {
                list.add(new Object[]{
                    plate,
                    rs.getTimestamp("entryTime").toString(),
                    currentFine,   // Current Fine (RM)
                    pastDebt,      // Past Debt (RM)
                    (currentFine + pastDebt), // Total Owed (RM)
                    "Unpaid/Overstayed"
                });
            }
        }
    } catch (SQLException e) {
        System.out.println("Error in getVehiclesWithFines: " + e.getMessage());
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

public void processExitWithPostponedFine(String plate) {
    Ticket ticket = Ticket.findActiveByPlate(plate);
    if (ticket == null) return;

    // 1. Calculate the fine for the current stay
    fineManager.setStrategy(ticket.getFineScheme());
    double currentFine = fineManager.calculateFine(ticket.calculateDurationHours());

    // 2. Save current fine to the license plate 'account' instead of the ticket
    if (currentFine > 0) {
        fineManager.postponeFineToAccount(plate, currentFine);
    }

    // 3. Close the ticket but record 0.00 paid for fines in the ticket record
    // This allows the car to leave while the 'vehicle' table remembers the debt
    ticketService.closeTicketAndPay(plate, 3.00, 0.00, "POSTPONED");
}


public double calculateTotalDue(String plate, int currentHours) {
    // 1. Get current fine based on active strategy (Option A, B, or C)
    double currentFine = fineManager.calculateFine(currentHours);
    
    // 2. Get the "Account" debt linked to the License Plate
    double historicalDebt = fineManager.getOutstandingFineByPlate(plate);
    
    // 3. Base parking fee (e.g., RM 3/hour)
    double parkingFee = currentHours * 3.00;
    
    return currentFine + historicalDebt + parkingFee;
}

public double calculateFinalBill(String plate) {
    Ticket ticket = ticketService.getActiveTicket(plate);
    
    // 1. Calculate current stay duration
    long hours = ticket.calculateDuration(); 
    double currentParkingFee = hours * 3.00; 

    // 2. Calculate current stay fine (if they overstayed > 24 hours)
    fineManager.setStrategy(ticket.getFineScheme());
    double currentFine = fineManager.calculateFine(hours); //

    // 3. Get the old debt that was linked to the plate
    double oldDebt = ticket.getCarriedOverFine(); 

    // 4. Return the grand total
    return currentParkingFee + currentFine + oldDebt; //
}


public void finalizeExit(String plate, double amountPaid, double totalDue) {
    if (amountPaid < totalDue) {
        double unpaidAmount = totalDue - amountPaid;
        // This moves the data to the permanent vehicle table
        fineManager.postponeFineToAccount(plate, unpaidAmount); 
    } else {
        // If they paid everything, clear the debt
        fineManager.resetAccountFines(plate); 
    }
    // Only close the ticket AFTER saving the debt
    ticketService.closeTicket(plate);
}


public List<Object[]> getAllOutstandingFines() {
    List<Object[]> data = new ArrayList<>();
    // This query finds ANY vehicle that owes money, even if they aren't parked now
    String sql = "SELECT licensePlate, outstandingFines FROM vehicle WHERE outstandingFines > 0";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            data.add(new Object[]{
                rs.getString("licensePlate"),
                "N/A (Historical)", // Entry Time
                0.0,                 // Current stay fine
                rs.getDouble("outstandingFines"), // The Past Fine
                rs.getDouble("outstandingFines"), // Total
                "Outstanding Debt"
            });
        }
    } catch (SQLException e) { e.printStackTrace(); }
    return data;
}


}
