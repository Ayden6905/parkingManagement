/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author ayden
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VehicleRepository {

    public void registerVehicle(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicle (licensePlate, vehicleType, outstandingFines) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE vehicleType = VALUES(vehicleType)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, vehicle.getLicensePlate());
            stmt.setString(2, vehicle.getVehicleType().name()); 
            stmt.setDouble(3, vehicle.getOutstandingFines());
            
            stmt.executeUpdate();
        }
    }

    // Find vehicle by license plate (for Exit && Fine checks)
    public Vehicle findVehicle(String licensePlate) throws SQLException {
        String sql = "SELECT * FROM vehicle WHERE licensePlate = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, licensePlate);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String typeStr = rs.getString("vehicleType");
                    double fines = rs.getDouble("outstandingFines");
                    VehicleFactory factory = new VehicleFactory();
                    return factory.createVehicle(typeStr, licensePlate, fines);
                }
            }
        }
        return null; 
    }
}
