/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

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
    private ParkingRepository repository;
    
    public ParkingSystemFacade() {
        this.fineManager = new FineManager();
        this.ticketService = new TicketService();
        this.repository = new ParkingRepository();

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
    // Make sure column names 'username' and 'password' match your dstabase
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
        // 1) Old fines 
        double existingDebt = checkExistingDebt(plate);

        // 2) Scheme locked at entry
        String activeScheme = getCurrentFineScheme();

        // 3) Validate spot exists
        ParkingSpot chosenSpot = ParkingLot.getInstance().findSpotById(spotId);
        if (chosenSpot == null) {
            return "Error: Spot not found.";
        }

        // misuse fine
        double misuseFine = 50.0;
        if (chosenSpot.getSpotType() == SpotType.RESERVED) {
            boolean isReservedSpot = (chosenSpot instanceof ReservedSpot) || chosenSpot.getSpotType() == SpotType.RESERVED;

            if (isReservedSpot) {
                ParkingRepository repo = new ParkingRepository();

                boolean hasReservation
                        = repo.hasValidReservationForSpotNow(plate, spotId);

                if (!hasReservation) {
                    FineManager fm = new FineManager();
                    fm.setStrategy(activeScheme);
                    misuseFine = fm.calculateFine(1);
                    existingDebt += misuseFine;
                }
            }
        } 

        // 5) CREATE TICKET
        ticketService.createTicket(
                plate,
                vehicleType,
                spotId,
                isHandicappedCardHolder,
                activeScheme,
                existingDebt
        );

        // 6) Mark spot OCCUPIED
        Vehicle v = new VehicleFactory().createVehicle(vehicleType, plate);
        v.setHandicappedCardHolder(isHandicappedCardHolder);
        chosenSpot.parkVehicle(v);

        // 7) Receipt
        Ticket ticket = Ticket.findActiveByPlate(plate);
        if (ticket != null) {
            String receipt = ticket.generateFormattedTicket();

            if (misuseFine > 0) {
                receipt += "\n❌ RESERVED SPOT MISUSE FINE (Scheme: "
                        + activeScheme + "): RM "
                        + String.format("%.2f", misuseFine);
            }

            if (existingDebt > 0) {
                receipt += "\n⚠️ UNPAID FINES DETECTED: RM "
                        + String.format("%.2f", existingDebt);
            }

            return receipt;
        }

    } catch (Exception e) {
        e.printStackTrace();
        return "Error during vehicle entry: " + e.getMessage();
    }

    return "Error: Failed to generate ticket.";
} 
    
public double checkExistingDebt(String plate) {
    if (plate == null || plate.trim().isEmpty()) return 0.0;
    return fineManager.getOutstandingFineByPlate(plate);
}    
    
   
    //change fine scheme
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

        return "Fixed"; 
    }

    //parking summary
    public ParkingSummary getParkingSummary(String plate) {
    Ticket ticket = Ticket.findActiveByPlate(plate);
    if (ticket == null) return null;

    // Calc duration
    int duration = ticket.calculateDurationHours();
    
    //Use the "historical" scheme applied when the car entered
    fineManager.setStrategy(ticket.getFineScheme());
    double currentFine = fineManager.calculateFine(duration);
    
    //  Fetch the debt that was carried over into this ticket
    double carriedOverFine = ticket.getCarriedOverFine(); 
    
    // Calc total fee
     double parkingFee = calculateParkingFee(ticket.getLicensePlate(), ticket.getSpotId(), duration);
     double historicalDebt = ticket.getCarriedOverFine();
    

    return new ParkingSummary(
            ticket.getTicketId(),
            plate,
            ticket.getEntryTime(),
            LocalDateTime.now(),
            duration,
            parkingFee,
            currentFine + historicalDebt,
             parkingFee + currentFine + historicalDebt
    );
}

    //available spots
    public List<String> getAvailableSpotsFor(String plate, String vehicleType, boolean cardHolder) {

        ParkingRepository repo = new ParkingRepository();

        // Build vehicle using REAL plate 
        Vehicle v = vehicleFactory.createVehicle(vehicleType, plate);
        v.setHandicappedCardHolder(cardHolder);

        // plate has a valid reservation, ONLY return reserved spot
        List<String> reserved = repo.getReservedSelectableSpotIds(plate);
        if (reserved != null && !reserved.isEmpty()) {

            List<String> filtered = new ArrayList<>();
            for (String id : reserved) {
                ParkingSpot ps = ParkingLot.getInstance().findSpotById(id);

                // keep strict rules here
                if (ps != null && ps.isAvailable() && ps.canParkVehicle(v)) {
                    filtered.add(id);
                }
            }
            if (!filtered.isEmpty()) {
                return filtered;
            }
        }

        // 2)  list:  Reserved + others, filtered by availability
        List<String> ids = new ArrayList<>();
        for (ParkingSpot s : ParkingLot.getInstance().getAvailableSpots(v, plate)) {
             if (s.isAvailable() && s.canParkVehicle(v)) {
                ids.add(s.getSpotId());
            }
        }
        return ids;
    }
    
        
        
public List<String> getReservedSpotsForPlate(String plate) { 
    return repository.getReservedSelectableSpotIds(plate);
}


    //payment
       public Receipt processPayment(String plate, double finePaid, String method) {
    Ticket ticket = Ticket.findActiveByPlate(plate);
    if (ticket == null) return null;

    int duration = ticket.calculateDurationHours();
    double parkingFee = calculateParkingFee(ticket.getLicensePlate(), ticket.getSpotId(), duration);
    double totalPaid = parkingFee + finePaid;

    // Finalize ticket
    LocalDateTime now = LocalDateTime.now();
    ticket.closeTicket(now, parkingFee, ticket.getFineAmount(), totalPaid, method);
    
    return new Receipt(
        1,              
        ticket,         
        parkingFee,     
        finePaid,       
        totalPaid,      
        method,         
        now             
    );
}
    
 
public int getAvailableSpotsByFloor(int floorNum) {
    ParkingRepository repo = new ParkingRepository();
    return repo.getAvailableCountByFloor(floorNum);
}
    
    public List<Object[]> getOccupancyDetailsByFloor(int floor) {
    List<Object[]> details = new ArrayList<>();
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
            
            // If there's no ticket but there IS a reservation, label it 'Reserved'
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
    
    
    

public void finalizeExit(String plate, double amountPaid, double totalDue) {
    if (amountPaid < totalDue) {
        double unpaidAmount = totalDue - amountPaid;
        fineManager.postponeFineToAccount(plate, unpaidAmount); 
    } else {
        //  paid everything, clear the debt
        fineManager.resetAccountFines(plate); 
    }
    // close the ticket AFTER saving the debt
    ticketService.closeTicket(plate);
}


public List<Object[]> getActiveFinesReport() {
    List<Object[]> report = new ArrayList<>();
    
    //  select records where they have overstayed (> 24 hours) OR have existing debt
    String sql = "SELECT t.licensePlate, v.vehicleType, t.spotId, t.entryTime, t.fineScheme, t.carriedOverFine " +
                 "FROM ticket t " +
                 "LEFT JOIN vehicle v ON t.licensePlate = v.licensePlate " +
                 "WHERE t.exitTime IS NULL " +
                 "AND (TIMESTAMPDIFF(HOUR, t.entryTime, NOW()) >= 24 OR t.carriedOverFine > 0)"; 

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        
        while (rs.next()) {
            java.sql.Timestamp entryTimestamp = rs.getTimestamp("entryTime");
            String timeDisplay = (entryTimestamp != null) ? entryTimestamp.toString() : "N/A";
            
            double liveFine = 0.0;
            if (entryTimestamp != null) {
                LocalDateTime entryTime = entryTimestamp.toLocalDateTime();
                long hoursParked = java.time.Duration.between(entryTime, LocalDateTime.now()).toHours();
                
                String scheme = rs.getString("fineScheme");
                fineManager.setStrategy(scheme != null ? scheme : "Fixed");
                
                // Fine apply > 24-hour limit
                if (hoursParked >= 24) {
                    liveFine = fineManager.calculateFine((int) hoursParked);
                }
            }

            double totalFineToShow = liveFine + rs.getDouble("carriedOverFine");

            report.add(new Object[]{
                rs.getString("licensePlate"),
                rs.getString("vehicleType") == null ? "Car" : rs.getString("vehicleType"),
                rs.getString("spotId"),
                timeDisplay,
                rs.getString("fineScheme") == null ? "Fixed" : rs.getString("fineScheme"),
                totalFineToShow
            });
        }
    } catch (SQLException e) { 
        System.err.println("Database Error in getActiveFinesReport: " + e.getMessage());
    }
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

        public double calculateParkingFee(Vehicle vehicle, ParkingSpot spot, int hours) {
    // reserved Spots are RM 10/hour
    if (spot instanceof ReservedSpot) {
        return hours * 10.0;
    }
     //Handicapped Logic: RM 2/hour (FREE if in a Handicapped spot)
    if (vehicle instanceof HandicappedVehicle) {
        // Requirement: FREE only if handicapped card holder parks in handicapped spot
        if (spot instanceof HandicappedSpot) {
            return 0.0;
        }
        // RM 2/hour
        return hours * 2.0;
    }

    //Compact Vehicles (Motorcycles) = RM 2/hour
    if (vehicle instanceof Motorcycle) {
        return hours * 2.0;
    }

    // Standard Cars and SUVs = RM 5/hour
    if (vehicle instanceof Car || vehicle instanceof SUV) {
        return hours * 5.0;
    }

    return hours * 5.0;
}
    
        //FINE REVENUE ANALYTICS 
public List<Object[]> getFineRevenueReport() {
    List<Object[]> report = new ArrayList<>();
    // fine from the current stay + debt carried over
    String sql = "SELECT fineScheme, COUNT(*), " +
                 "SUM(fineAmount + carriedOverFine) as totalCollected, " +
                 "AVG(fineAmount + carriedOverFine) as avgFine " +
                 "FROM ticket WHERE (fineAmount > 0 OR carriedOverFine > 0) AND exitTime IS NOT NULL " +
                 "GROUP BY fineScheme";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            report.add(new Object[]{
                rs.getString("fineScheme"), 
                rs.getInt(2), 
                String.format("%.2f", rs.getDouble("totalCollected")), 
                String.format("%.2f", rs.getDouble("avgFine"))
            });
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return report;
}


    
    
    public List<Ticket> getRevenueReport() {
    return repository.getCompletedTickets(); 
}
        
}
