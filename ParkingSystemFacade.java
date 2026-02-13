/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author NurqistinaAtashah
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ParkingSystemFacade {
    
    // --- Member 2: Logic Modules ---
    private VehicleRepository vehicleRepo;
    private ParkingRepository parkingRepo;
    
    // --- Member 4: Existing Modules ---
    private FineManager fineManager;

    public ParkingSystemFacade() {
        // 1. Initialize Database Repositories
        this.vehicleRepo = new VehicleRepository();
        this.parkingRepo = new ParkingRepository();
        
        // 2. Initialize Fine Manager
        this.fineManager = new FineManager();
        
        // 3. LOAD SAVED SCHEME FROM DB
        // This ensures the system remembers the Admin's choice even after restarting.
        String savedScheme = getCurrentFineScheme();
        this.fineManager.setStrategy(savedScheme);
        System.out.println("System loaded with Fine Scheme: " + savedScheme);
    }

    // --- UTILITY: Check DB Connection ---
    public boolean checkDatabaseConnection() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ============================================================
    //       MEMBER 4's WORK (Admin Login)
    // ============================================================
    public boolean authenticateAdmin(String username, String password) {
        String query = "SELECT * FROM admin WHERE username = ? AND password = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Returns true if credentials match in DB
            }
            
        } catch (SQLException e) {
            System.out.println("Login Error: " + e.getMessage());
            return false;
        }
    }

    // ============================================================
    //       MEMBER 2's WORK (Vehicle Entry)
    // ============================================================
    public String handleVehicleEntry(String plate, String typeStr) {
        // 1. Input Validation
        if (plate == null || plate.trim().isEmpty()) return "Error: License plate required.";

        try {
            // 2. Create Vehicle Object (Using your Factory)
            Vehicle vehicle = SimpleVehicleFactory.createVehicle(plate, typeStr);

            // 3. Check Database for Available Spot (Using your Repository)
            String spotId = parkingRepo.findAvailableSpot(vehicle.getVehicleType());
            if (spotId == null) {
                return "Full: No available spots for " + typeStr + ".";
            }

            // 4. Register Vehicle in DB (if it's new)
            vehicleRepo.registerVehicle(vehicle);

            // 5. Perform Parking (Transaction: Update Spot + Create Ticket)
            boolean success = parkingRepo.parkVehicle(vehicle, spotId);

            if (success) {
                return "Success! Vehicle parked at Spot: " + spotId;
            } else {
                return "Error: Database transaction failed.";
            }

        } catch (IllegalArgumentException e) {
            return "Error: Invalid vehicle type selected.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Database connection issue. Check console for details.";
        }
    }

    // ============================================================
    //       EXIT LOGIC (Placeholder / Stub)
    // ============================================================
    public Receipt handleVehicleExit(String plate) {
        // Keeping this as a stub so the code compiles.
        // We will replace this with real database logic in the next step!
        
        int hoursParked = 2;  
        double hourlyRate = 3.00;
        
        // Corrected: Passing 'long' (hours) instead of String
        double fine = fineManager.calculateFine(hoursParked); 
        
        double totalPaid = (hoursParked * hourlyRate) + fine;
        return new Receipt(plate, hoursParked, totalPaid);
    }

    // ============================================================
    //       MEMBER 2: FINE SCHEME MANAGEMENT (Admin Features)
    // ============================================================

    // 1. GET Current Scheme (To display to Admin)
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
        return "Fixed"; // Default fallback
    }

    // 2. SET New Scheme (When Admin clicks a button)
    public boolean changeSystemFineScheme(String schemeName) {
        // A. Update the Database
        String sql = "UPDATE fineStrategy SET current_scheme = ? WHERE id = 1";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, schemeName);
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                // B. Update the Live Logic
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
}