/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parkingmanagement;
/**
 *
 * @author ayden
 */
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
    
    public List<ParkingSpot> getAllParkingSpots() {

        List<ParkingSpot> list = new ArrayList<>();
        String sql = "SELECT * FROM parkingSpot";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String spotId = rs.getString("spotId");
                String spotType = rs.getString("spotType");
                String statusStr = rs.getString("status");             
                int floorNumber = rs.getInt("floorNumber");

                ParkingSpot spot;

                switch (spotType.toUpperCase()) {
                    case "COMPACT":
                        spot = new CompactSpot(spotId, floorNumber);
                        break;
                    case "REGULAR":
                        spot = new RegularSpot(spotId, floorNumber);
                        break;
                    case "HANDICAPPED":
                        spot = new HandicappedSpot(spotId, floorNumber);
                        break;
                    case "RESERVED":
                        spot = new ReservedSpot(spotId, floorNumber);
                        break;
                    default:
                        throw new RuntimeException("Unknown spot type: " + spotType);
                }

                spot.setStatus(SpotStatus.valueOf(statusStr.toUpperCase()));
                list.add(spot);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
    
    public boolean createReservation(Reservation r) {
        String sql = "INSERT INTO reservation (reservationId, plate, spotId, startTime, endTime, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, r.getReservationId());
            ps.setString(2, r.getLicensePlate());
            ps.setString(3, r.getSpotId().getSpotId());
            ps.setTimestamp(4, Timestamp.valueOf(r.getStartTime()));
            ps.setTimestamp(5, Timestamp.valueOf(r.getEndTime()));
            ps.setString(6, r.getStatus().name());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Add this to ParkingRepository.java
public boolean releaseSpot(String spotId) {
    String sql = "UPDATE parkingSpot SET status = 'Available' WHERE spotId = ?";
    
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setString(1, spotId);
        return ps.executeUpdate() == 1;
        
    } catch (SQLException e) {
        System.out.println("Error releasing spot: " + e.getMessage());
        return false;
    }
} 

// Add this to ParkingRepository.java
public int getAvailableCountByFloor(int floorNum) {
    // This query links reservations specifically to the floor of the spot
    String sql = "SELECT COUNT(*) FROM parkingSpot p " +
                 "WHERE p.floorNumber = ? " +
                 "AND p.spotId NOT IN (SELECT t.spotId FROM ticket t WHERE t.exitTime IS NULL) " +
                 "AND p.spotId NOT IN (SELECT r.spotId FROM reservation r " +
                                    "JOIN parkingSpot ps ON r.spotId = ps.spotId " +
                                    "WHERE r.status = 'ACTIVE' AND ps.floorNumber = ?)";
    
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setInt(1, floorNum);
        ps.setInt(2, floorNum); // Bind floorNum twice to satisfy both '?'
        
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}



public List<Object[]> getOccupancyDetailsByFloor(int floor) {
        List<Object[]> details = new ArrayList<>();
        // Use COALESCE to pick the first non-null plate and time from either ticket or reservation
        String sql = "SELECT p.spotId, p.spotType, p.status, " +
                     "COALESCE(t.licensePlate, r.plate) AS vehicleNo, " + 
                     "COALESCE(t.entryTime, r.startTime) AS time " +     
                     "FROM parkingSpot p " +
                     "LEFT JOIN ticket t ON p.spotId = t.spotId AND t.exitTime IS NULL " +
                     "LEFT JOIN reservation r ON p.spotId = r.spotId AND r.status = 'ACTIVE' " +
                     "WHERE p.floorNumber = ? " +
                     "ORDER BY p.spotId ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, floor);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String dbStatus = rs.getString("status");
                String vehicleNo = rs.getString("vehicleNo");
                
                // Determine display status: prioritize "Reserved" label if a booking exists
                String displayStatus = dbStatus;
                if (vehicleNo != null && !"Occupied".equalsIgnoreCase(dbStatus)) {
                    displayStatus = "Reserved";
                }

                details.add(new Object[]{
                    rs.getString("spotId"),
                    rs.getString("spotType"),
                    displayStatus,
                    vehicleNo == null ? "-" : vehicleNo,
                    rs.getTimestamp("time") == null ? "-" : rs.getTimestamp("time").toString()
                });
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return details;
    }
}

