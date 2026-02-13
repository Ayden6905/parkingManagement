/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author ayden
 */
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class ParkingRepository {

    // 1. Matches vehicle type to allowed spot types
    public String findAvailableSpot(VehicleType type) throws SQLException {
        
        String allowedTypes;
        
        // Determine which spots are allowed based on vehicle type
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
                // Handicapped vehicles can park in ANY spot type
                allowedTypes = "'Compact', 'Regular', 'Handicapped', 'Reserved'";
                break;
            default:
                return null;
        }

        // String.format to build the query safely
        String sql = "SELECT spotId FROM parkingSpot " +
                     "WHERE status = 'Available' AND spotType IN (" + allowedTypes + ") " +
                     "LIMIT 1";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getString("spotId");
            }
        }
        return null; // No spots found
    }

    // 2. Updates spot status AND creates a ticket
    public boolean parkVehicle(Vehicle vehicle, String spotId) {
        String updateSpotSql = "UPDATE parkingSpot SET status = 'Occupied' WHERE spotId = ?";
        String insertTicketSql = "INSERT INTO ticket (ticketId, licensePlate, spotId, entryTime) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION

            // Step A: Mark spot as Occupied
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSpotSql)) {
                updateStmt.setString(1, spotId);
                updateStmt.executeUpdate();
            }

            // Step B: Generate Ticket
            try (PreparedStatement ticketStmt = conn.prepareStatement(insertTicketSql)) {
                // Ticket ID Format: T-PLATE-TIMESTAMP
                String timestampStr = vehicle.getEntryTime().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
                String ticketId = "T-" + vehicle.getLicensePlate() + "-" + timestampStr;

                ticketStmt.setString(1, ticketId);
                ticketStmt.setString(2, vehicle.getLicensePlate());
                ticketStmt.setString(3, spotId);
                ticketStmt.setTimestamp(4, Timestamp.valueOf(vehicle.getEntryTime()));
                
                ticketStmt.executeUpdate();
            }

            conn.commit(); // COMMIT TRANSACTION
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
