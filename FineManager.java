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

public class FineManager {
    
    public double getOutstandingFineByPlate(String plate) {
        String sql = "SELECT outstandingFines FROM vehicle WHERE licensePlate = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
        
            ps.setString(1, plate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("outstandingFines");
                }
            }
    
        } catch (SQLException e) {
            System.out.println("Error fetching fine: " + e.getMessage());
        }
        return 0.0;
    }
}