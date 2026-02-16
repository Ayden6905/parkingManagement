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
    private VehicleFactory vehicleFactory = new VehicleFactory();

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
    // Make sure column names 'username' and 'password' match your DB exactly!
    String sql = "SELECT * FROM admin WHERE username = ? AND password = ?";
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setString(1, username);
        ps.setString(2, password);
        
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next(); // True if record exists
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

    //vehicle entry
    public String handleVehicleEntry(String plate, String vehicleType, String spotId, boolean isHandicappedCardHolder) {
        if (plate == null || plate.trim().isEmpty()) {
            return "Error: License plate required.";
        }

        // Check if car is already inside
        if (Ticket.findActiveByPlate(plate) != null) {
            return "Error: Vehicle with plate " + plate + " is already inside.";
        }

        try {
            // 1) Old fines already in account
            double existingDebt = checkExistingDebt(plate);

            // 2) Scheme locked at entry (this gets stored into ticket)
            String activeScheme = getCurrentFineScheme();

            // 3) Validate spot exists
            ParkingSpot chosenSpot = ParkingLot.getInstance().findSpotById(spotId);
            if (chosenSpot == null) {
                return "Error: Spot not found.";
            }

            // 4) RESERVED SPOT MISUSE fine (scheme-based, locked at entry)
            double misuseFine = 0.0;

            if (chosenSpot.getSpotType() == SpotType.RESERVED) {
                ParkingRepository repo = new ParkingRepository();
                boolean hasReservation = repo.hasValidReservationForSpotNow(plate, spotId);

                if (!hasReservation) {
                    // Use the entry scheme to compute this fine (per driver)
                    FineManager tempFineManager = new FineManager();
                    tempFineManager.setStrategy(activeScheme);
                    
                    misuseFine = tempFineManager.calculateFine(1);

                    // Save it into driver's account immediately
                    tempFineManager.postponeFineToAccount(plate, misuseFine);

                    // Update the debt shown on receipt + stored into ticket
                    existingDebt += misuseFine;
                }
            }

            // 5) Create ticket (stores schemeUsedAtEntry + current debt)
            ticketService.createTicket(plate, vehicleType, spotId, isHandicappedCardHolder, activeScheme, existingDebt);

            // 6) Update in-memory spot so OCCUPIED spots disappear from lists
            ParkingSpot spot = ParkingLot.getInstance().findSpotById(spotId);
            if (spot != null) {
                Vehicle v = new VehicleFactory().createVehicle(vehicleType, plate);
                v.setHandicappedCardHolder(isHandicappedCardHolder);
                spot.parkVehicle(v); // sets currentVehicle + OCCUPIED
            }

            // 7) Receipt
            Ticket ticket = Ticket.findActiveByPlate(plate);
            if (ticket != null) {
                String receipt = ticket.generateFormattedTicket();

                if (misuseFine > 0) {
                    receipt += "\n❌ RESERVED SPOT MISUSE FINE (Scheme: " + activeScheme + "): RM "
                            + String.format("%.2f", misuseFine);
                }

                if (existingDebt > 0) {
                    receipt += "\n⚠️ UNPAID FINES DETECTED: RM " + String.format("%.2f", existingDebt);
                }

                return receipt;
            }

        } catch (Exception e) {
            return "Error during vehicle entry: " + e.getMessage();
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
    public ParkingSummary getParkingSummary(String plate, double baseHourlyRate) {
    Ticket ticket = Ticket.findActiveByPlate(plate);
    if (ticket == null) return null;

    // 1. Calculate duration
    int duration = ticket.calculateDurationHours();
    
    // 2. Use the "historical" scheme applied when the car entered
    fineManager.setStrategy(ticket.getFineScheme());
    double currentFine = fineManager.calculateFine(duration);
    
    // 3. IMPORTANT: Fetch the debt that was carried over into this ticket
    double carriedOverFine = ticket.getCarriedOverFine(); 
    
    // 4. Calculate total fee
    double parkingFee = duration * baseHourlyRate;
    double totalDue = parkingFee + currentFine + carriedOverFine;

    return new ParkingSummary(
            ticket.getTicketId(),
            plate,
            ticket.getEntryTime(),
            LocalDateTime.now(),
            duration,
            parkingFee,
            currentFine + carriedOverFine, // Total Fines (Current + Past)
            totalDue
    );
}

    //available spots
    public List<String> getAvailableSpotsFor(String plate, String vehicleType, boolean cardHolder) {

        ParkingRepository repo = new ParkingRepository();

        // Build vehicle using REAL plate (no TEMP)
        Vehicle v = vehicleFactory.createVehicle(vehicleType, plate);
        v.setHandicappedCardHolder(cardHolder);

        // 1) If plate has a valid reservation -> ONLY return reserved spot(s)
        List<String> reserved = repo.getReservedSelectableSpotIds(plate);
        if (reserved != null && !reserved.isEmpty()) {

            // Filter: only show if the reserved spot is currently AVAILABLE & can park vehicle
            List<String> filtered = new ArrayList<>();
            for (String id : reserved) {
                ParkingSpot ps = ParkingLot.getInstance().findSpotById(id);
                if (ps != null && ps.isAvailable() && ps.canParkVehicle(v)) {
                    filtered.add(id);
                }
            }
            return filtered;
        }

        // 2) Otherwise -> normal list (includes Reserved + others, filtered by availability)
        List<String> ids = new ArrayList<>();
        for (ParkingSpot s : ParkingLot.getInstance().getAvailableSpots(v, plate)) {
            ids.add(s.getSpotId());
        }
        return ids;
    }

    
    //for reservation spot
    public List<String> getReservedSpotsForPlate(String plate) {
        ParkingRepository repo = new ParkingRepository();
        return repo.getReservedSelectableSpotIds(plate);
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
    
 
    
    // Inside ParkingSystemFacade.java
public int getAvailableSpotsByFloor(int floorNum) {
    ParkingRepository repo = new ParkingRepository();
    return repo.getAvailableCountByFloor(floorNum);
}
    
    public List<Object[]> getOccupancyDetailsByFloor(int floor) {
    List<Object[]> details = new ArrayList<>();
    // Updated SQL to join both ticket AND reservation tables
    String sql = "SELECT p.spotId, p.spotType, p.status, " +
                 "COALESCE(t.licensePlate, r.plate) AS vehicleNo, " + // Get plate from ticket or reservation
                 "COALESCE(t.entryTime, r.startTime) AS time " +     // Get time from ticket or reservation
                 "FROM parkingSpot p " +
                 "LEFT JOIN ticket t ON p.spotId = t.spotId AND t.exitTime IS NULL " +
                 "LEFT JOIN reservation r ON p.spotId = r.spotId AND r.status = 'ACTIVE' " +
                 "WHERE p.floorNumber = ? " +
                 "ORDER BY p.spotId ASC";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setInt(1, floor);
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            String dbStatus = rs.getString("status");
            String vehicleNo = rs.getString("vehicleNo");
            
            // Logic: If there's no ticket but there IS a reservation, label it 'Reserved'
            String displayStatus = dbStatus;
            if (vehicleNo != null && !"Occupied".equalsIgnoreCase(dbStatus)) {
                displayStatus = "Reserved";
            }

            details.add(new Object[]{
                rs.getString("spotId"),
                rs.getString("spotType"),
                displayStatus,
                vehicleNo == null ? "-" : vehicleNo,
                rs.getTimestamp("time") == null ? "-" : rs.getTimestamp("time").toString()
            });
        }
    } catch (SQLException e) { 
        e.printStackTrace(); 
    }
    return details;
}
    
    public List<Object[]> getVehiclesWithFines() {
    List<Object[]> data = new ArrayList<>();
    
    // We JOIN vehicle and ticket to see both active status and historical debt
    String sql = "SELECT v.licensePlate, t.entryTime, t.carriedOverFine AS currentTicketFine, " +
                 "v.outstandingFines AS historicalDebt, v.vehicleType " +
                 "FROM vehicle v " +
                 "LEFT JOIN ticket t ON v.licensePlate = t.licensePlate AND t.exitTime IS NULL " +
                 "WHERE v.outstandingFines > 0 OR t.carriedOverFine > 0";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            double currentFine = rs.getDouble("currentTicketFine");
            double pastDebt = rs.getDouble("historicalDebt");
            double total = currentFine + pastDebt;
            String status = (rs.getTimestamp("entryTime") != null) ? "PARKED" : "AWAY";
            String entryTime = (rs.getTimestamp("entryTime") != null) ? 
                               rs.getTimestamp("entryTime").toString() : "N/A";

            data.add(new Object[]{
                rs.getString("licensePlate"),
                entryTime,
                currentFine,
                pastDebt,
                total,
                status
            });
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return data;
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

public List<Object[]> getActiveFinesReport() {
    List<Object[]> report = new ArrayList<>();
    // Using LEFT JOIN ensures the ticket shows even if vehicle details are missing
    String sql = "SELECT t.licensePlate, v.vehicleType, t.spotId, t.entryTime, t.fineScheme, t.carriedOverFine " +
                 "FROM ticket t " +
                 "LEFT JOIN vehicle v ON t.licensePlate = v.licensePlate " + // Changed to LEFT JOIN
                 "WHERE t.exitTime IS NULL AND t.carriedOverFine > 0";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            report.add(new Object[]{
                rs.getString("licensePlate"),
                rs.getString("vehicleType"),
                rs.getString("spotId"),
                rs.getTimestamp("entryTime").toString(),
                rs.getString("fineScheme"), // <--- New data point
                rs.getDouble("carriedOverFine")
            });
        }
    } catch (SQLException e) { e.printStackTrace(); }
    return report;
}


public List<Object[]> getPastDebtReport() {
    List<Object[]> report = new ArrayList<>();
    // This query gets the vehicle info and joins with the latest ticket to find the scheme
    String sql = "SELECT v.licensePlate, v.vehicleType, v.outstandingFines, " +
                 "(SELECT t.fineScheme FROM ticket t WHERE t.licensePlate = v.licensePlate ORDER BY t.entryTime DESC LIMIT 1) as lastScheme " +
                 "FROM vehicle v WHERE v.outstandingFines > 0";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            report.add(new Object[]{
                rs.getString("licensePlate"),
                rs.getString("vehicleType"),
                rs.getString("lastScheme") != null ? rs.getString("lastScheme") : "N/A",
                rs.getDouble("outstandingFines")
            });
        }
    } catch (SQLException e) { 
        e.printStackTrace(); 
    }
    return report;
}


    //newly added
    public double calculateParkingFee(Vehicle v, ParkingSpot spot, int hours) {

        // for handicapped driver
        if (v.isHandicappedCardHolder()) {
            
            if (spot.getSpotType() == SpotType.HANDICAPPED) {
                return 0.0;
            }
            
            if (spot.getSpotType() == SpotType.REGULAR) {
                return 2.0;                
            }
        }
        return hours * spot.getHourlyRate();
    }
    
    
    public Ticket parkVehicle(Vehicle v, ParkingSpot spot, String scheme) {
    // 1. Update the physical spot status (makes it occupied in the UI/Dashboard)
    spot.parkVehicle(v);
    
    // 2. Extract the plate string (assuming Vehicle class has getLicensePlate())
    // If your compiler complains, check if the method is getPlate() instead
    String plate = v.getLicensePlate(); 

    // 3. Reuse your existing handleVehicleEntry logic to update DB and check debt
    // This will generate the actual database record in the 'ticket' table
    handleVehicleEntry(
        plate, 
        v.getClass().getSimpleName(), 
        spot.getSpotId(), 
        v.isHandicappedCardHolder()
    );

    // 4. Return the Ticket object so the EntryPanel knows it was successful
    return Ticket.findActiveByPlate(plate);
}
    

}
