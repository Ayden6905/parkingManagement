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
    
    private FineStrategy currentStrategy;
    
    public FineManager() {
        this.currentStrategy = new FixedFineStrategy();
    }
    
    public void setStrategy(String schemeName) {
        switch (schemeName) {
            case "Fixed":
                this.currentStrategy = new FixedFineStrategy();
                break;
            case "Progressive":
                this.currentStrategy = new ProgressiveFineStrategy();
                break;
            case "Hourly":
                this.currentStrategy = new HourlyFineStrategy();
                break;
            default:
                System.out.println("Unknown scheme: " + schemeName + ". Keeping current strategy.");
        }
    }
    
    public double calculateFine(long totalHours) {
        return currentStrategy.calculateFine(totalHours);
    }
    
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
    //Saves a fine to the user's account if they don't pay immediately at exit
    public void postponeFineToAccount(String plate, double amount) {
    // inserts a new row or adds the amount to the existing balance.
    String sql = "INSERT INTO vehicle (licensePlate, vehicleType, outstandingFines) " +
                 "VALUES (?, 'Car', ?) " +
                 "ON DUPLICATE KEY UPDATE outstandingFines = outstandingFines + ?";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setString(1, plate);
        ps.setDouble(2, amount); // Initial value if new
        ps.setDouble(3, amount); // Add to value if exists
        
        int rows = ps.executeUpdate();
        System.out.println("Data saved to vehicle table. Rows affected: " + rows);
    } catch (SQLException e) {
        System.out.println("CRITICAL ERROR: Could not save fine! " + e.getMessage());
    }
}
    
    public void resetAccountFines(String plate) {
        String sql = "UPDATE vehicle SET outstandingFines = 0.0 WHERE licensePlate = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
}