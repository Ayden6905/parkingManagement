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
}