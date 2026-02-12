/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author User
 */
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PaymentService {
    
    public void recordPayment(String ticketId, String plate, double amount, String paymentMethod, double finePaid) {
        String sql = "INSERT INTO payment (ticketId, amount, paymentMethod) VALUES (?, ?, ?)";
        String sqlUpdateVehicle = "UPDATE vehicle SET outstandingFines = outstandingFines - ? WHERE licensePlate = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, ticketId);
            ps.setDouble(2, amount);
            ps.setString(3, paymentMethod);
            ps.executeUpdate();
        }
        
        catch (SQLException e) {
            System.out.println("Error recording payment: " + e.getMessage());
        }
    }
    
    public void createReceipt(String ticektId, double parkingFee, double fineAmount, double totalPaid) {
        String sql = "INSERT INTO receipt (ticketId, parkingFee, fineAmount, totalPaid) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ticketId);
            ps.setDouble(2, parkingFee);
            ps.setDouble(3, fineAmount);
            ps.setDouble(4, totalPaid);
            ps.executeUpdate();
    }
        
    catch (SQLException e) {
        System.out.println("Error creating receipt: " + e.getMessage());
    }
  }
}
