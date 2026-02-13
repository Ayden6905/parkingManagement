/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author ayden
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class VehicleRepository {

    // 1. SAVE/UPDATE a vehicle in the database
    // Uses "INSERT ... ON DUPLICATE KEY UPDATE" so if the car comes back, we just update it.
    public void registerVehicle(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicle (licensePlate, vehicleType, outstandingFines) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE vehicleType = VALUES(vehicleType)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, vehicle.getLicensePlate());
            // .name() converts the Enum (e.g., VehicleType.CAR) to string "CAR"
            stmt.setString(2, vehicle.getVehicleType().name()); 
            stmt.setDouble(3, vehicle.getOutstandingFines());
            
            stmt.executeUpdate();
        }
    }

    // 2. Find vehicle by license plate (for Exit && Fine checks)
    public Vehicle findVehicle(String licensePlate) throws SQLException {
        String sql = "SELECT * FROM vehicle WHERE licensePlate = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, licensePlate);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String typeStr = rs.getString("vehicleType");
                    double fines = rs.getDouble("outstandingFines");
                    
                    // Use Factory to recreate the object
                    Vehicle v = SimpleVehicleFactory.createVehicle(licensePlate, typeStr);
                    v.addFine(fines); // Restore old fines
                    return v;
                }
            }
        }
        return null; // Not found
    }
}
