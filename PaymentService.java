/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parkingmanagement;

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
        String sqlPayment = "INSERT INTO payment (ticketId, amount, paymentMethod) VALUES (?, ?, ?)";
        String sqlUpdateVehicle = "UPDATE vehicle SET outstandingFines = outstandingFines - ? WHERE licensePlate = ?";
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false); 
            try (PreparedStatement ps1 = conn.prepareStatement(sqlPayment);
                 PreparedStatement ps2 = conn.prepareStatement(sqlUpdateVehicle)) {
                
                ps1.setString(1, ticketId);
                ps1.setDouble(2, amount);
                ps1.setString(3, paymentMethod);
                ps1.executeUpdate();

                ps2.setDouble(1, finePaid);
                ps2.setString(2, plate);
                ps2.executeUpdate();
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Payment Error: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }
    
    public void createReceipt(String ticketId, double parkingFee, double fineAmount, double totalPaid) {
        String sql = "INSERT INTO receipt (ticketId, parkingFee, fineAmount, totalPaid) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticketId);
            ps.setDouble(2, parkingFee);
            ps.setDouble(3, fineAmount);
            ps.setDouble(4, totalPaid);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving receipt: " + e.getMessage());
        }
    }
}
