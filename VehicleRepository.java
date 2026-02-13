/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingmanagement;

/**
 *
 * @author ayden
 */
import java.sql.*;

public class VehicleRepository {

    // Ensures the vehicle is in the database. 
    // Uses "INSERT IGNORE" or "ON DUPLICATE KEY UPDATE" logic.
    public void registerVehicle(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicle (licensePlate, vehicleType, outstandingFines) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE vehicleType = VALUES(vehicleType)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, vehicle.getLicensePlate());
            stmt.setString(2, vehicle.getVehicleType().toString()); // Matches ENUM
            stmt.setDouble(3, 0.00); // Default fines
            
            stmt.executeUpdate();
        }
    }
}
