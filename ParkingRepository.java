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
import java.time.format.DateTimeFormatter;

public class ParkingRepository {

    // 1. Logic to find a suitable spot based on Vehicle Type
    public String findAvailableSpot(VehicleType type) throws SQLException {
        String sql = "SELECT spotId FROM parkingSpot WHERE status = 'Available' AND spotType IN (%s) LIMIT 1";
        
        // Logic mapping Vehicle -> Allowed Spot Types (Based on your PDF)
        String allowedTypes;
        switch (type) {
            case MOTORCYCLE:
                allowedTypes = "'Compact'";
                break;
            case CAR:
                allowedTypes = "'Compact', 'Regular'";
                break;
            case SUV:
                allowedTypes = "'Regular'";
                break;
            case HANDICAPPED:
                allowedTypes = "'Compact', 'Regular', 'Handicapped', 'Reserved'";
                break;
            default:
                return null;
        }

        // Format the query safely
        String finalQuery = String.format(sql, allowedTypes);

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(finalQuery)) {
            
            if (rs.next()) {
                return rs.getString("spotId");
            }
        }
        return null; // No spot found
    }

    // 2. Occupy the spot and create the ticket (Transaction)
    public boolean parkVehicle(Vehicle vehicle, String spotId) {
        String updateSpotSql = "UPDATE parkingSpot SET status = 'Occupied' WHERE spotId = ?";
        String insertTicketSql = "INSERT INTO ticket (ticketId, licensePlate, spotId, entryTime) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // Step A: Update Spot Status
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSpotSql)) {
                updateStmt.setString(1, spotId);
                updateStmt.executeUpdate();
            }

            // Step B: Create Ticket
            try (PreparedStatement ticketStmt = conn.prepareStatement(insertTicketSql)) {
                // Generate Ticket ID: T-PLATE-TIMESTAMP
                String timestamp = vehicle.getEntryTime().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
                String ticketId = "T-" + vehicle.getLicensePlate() + "-" + timestamp;

                ticketStmt.setString(1, ticketId);
                ticketStmt.setString(2, vehicle.getLicensePlate());
                ticketStmt.setString(3, spotId);
                ticketStmt.setTimestamp(4, Timestamp.valueOf(vehicle.getEntryTime()));
                
                ticketStmt.executeUpdate();
            }

            conn.commit(); // Commit Transaction
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}