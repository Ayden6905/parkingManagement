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
import java.util.Date;

public class ParkingSystemFacade {
    private ParkingLot parkingLot;
    private FineManager fineManager;

    public ParkingSystemFacade() {
        this.parkingLot = new ParkingLot(5); 
        this.fineManager = new FineManager();
    }

    public boolean checkDatabaseConnection() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Member 4: This is the logic that makes your button work!
    public boolean authenticateAdmin(String username, String password) {
        String query = "SELECT * FROM admins WHERE username = ? AND password = ?";
        
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

    public String handleVehicleEntry(String plate, String type) {
        return "Ticket Generated: T-" + plate; 
    }

    public Receipt handleVehicleExit(String plate) {
        int hoursParked = 2;  
        double hourlyRate = 3.00;
        double fine = fineManager.calculateFine("NONE"); 
        double totalPaid = (hoursParked * hourlyRate) + fine;
        return new Receipt(plate, hoursParked, totalPaid);
    }

    public void changeSystemFineScheme(String schemeName) {
        System.out.println("Switching scheme to: " + schemeName);
    }
}