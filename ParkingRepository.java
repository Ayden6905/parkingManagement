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
import java.util.ArrayList;
import java.util.List;

public class ParkingRepository {

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
                allowedTypes = "'Compact', 'Regular', 'Handicapped', 'Reserved'";
                break;
            default:
                return null;
        }

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
        return null; 
    }

    //Updates spot status AND creates a ticket 
    public boolean parkVehicle(Vehicle vehicle, String spotId) {

        String updateSpotSql
                = "UPDATE parkingSpot SET status = 'Occupied' "
                + "WHERE spotId = ? AND status = 'Available'";

        String insertTicketSql
                = "INSERT INTO ticket (ticketId, licensePlate, spotId, entryTime) VALUES (?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); 

            // Step A: Mark spot as Occupied ONLY if still Available
            int updated;
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSpotSql)) {
                updateStmt.setString(1, spotId);
                updated = updateStmt.executeUpdate();
            }

            // If no row updated, spot not available (already taken)
            if (updated == 0) {
                conn.rollback();
                return false;
            }

            // Step B: Insert ticket
            try (PreparedStatement ticketStmt = conn.prepareStatement(insertTicketSql)) {
                String timestampStr = vehicle.getEntryTime()
                        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
                String ticketId = "T-" + vehicle.getLicensePlate() + "-" + timestampStr;

                ticketStmt.setString(1, ticketId);
                ticketStmt.setString(2, vehicle.getLicensePlate());
                ticketStmt.setString(3, spotId);
                ticketStmt.setTimestamp(4, Timestamp.valueOf(vehicle.getEntryTime()));

                ticketStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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

public int getAvailableCountByFloor(int floorNum) {
    String sql = "SELECT COUNT(*) FROM parkingSpot p " +
                 "WHERE p.floorNumber = ? " +
                 "AND p.spotId NOT IN (SELECT t.spotId FROM ticket t WHERE t.exitTime IS NULL) " +
                 "AND p.spotId NOT IN (SELECT r.spotId FROM reservation r " +
                                    "JOIN parkingSpot ps ON r.spotId = ps.spotId " +
                                    "WHERE r.status = 'ACTIVE' AND ps.floorNumber = ?)";
    
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        ps.setInt(1, floorNum);
        ps.setInt(2, floorNum); 
        
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

    public List<String> getReservedSelectableSpotIds(String plate) {
        List<String> ids = new ArrayList<>();

        String sql
                = "SELECT spotId "
                + "FROM reservation "
                + "WHERE UPPER(TRIM(plate)) = ? "
                + "  AND UPPER(status) = 'ACTIVE' "
                + "  AND NOW() <= endTime";   

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, plate.trim().toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getString("spotId"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }
    
    public void expirePastReservations() {
        String sql
                = "UPDATE reservation "
                + "SET status = 'EXPIRED' "
                + "WHERE UPPER(status) = 'ACTIVE' "
                + "  AND endTime < NOW()";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<String> getSelectableSpotIds(String plate, String vehicleType, boolean isCardHolder) {
        List<String> ids = new ArrayList<>();

        // 1) Check plate has an active reservation
        List<String> reservedIds = getReservedSelectableSpotIds(plate);
        boolean hasReservation = !reservedIds.isEmpty();

        // 2) fetch AVAILABLE spots from DB
        String sql = "SELECT spotId, spotType FROM parkingSpot WHERE status = 'Available'";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            VehicleFactory vf = new VehicleFactory();
            Vehicle v = vf.createVehicle(vehicleType, "TEMP");
            v.setHandicappedCardHolder(isCardHolder);

            while (rs.next()) {
                String spotId = rs.getString("spotId");
                String spotType = rs.getString("spotType");

                //Reserved rule
                if ("Reserved".equalsIgnoreCase(spotType) || "RESERVED".equalsIgnoreCase(spotType)) {
                    // show reserved if this plate reserved it AND within valid time
                    if (!reservedIds.contains(spotId)) {
                        continue;
                    }
                } else {
                    // If they DO have a reservation, see ONLY reserved options
                    if (hasReservation) {
                        continue;
                    }
                }

                if (isCompatible(vehicleType, isCardHolder, spotType)) {
                    ids.add(spotId);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    private boolean isCompatible(String vehicleType, boolean isCardHolder, String spotType) {
        String vt = vehicleType.toUpperCase();
        String st = spotType.toUpperCase();

        if (isCardHolder || vt.equals("HANDICAPPED")) {
            return true;
        }

        switch (vt) {
            case "MOTORCYCLE":
                return st.equals("COMPACT");
            case "CAR":
                return st.equals("COMPACT") || st.equals("REGULAR");
            case "SUV":
                return st.equals("REGULAR");
            default:
                return false;
        }
    }
    
    public boolean occupySpot(String spotId) {
        String sql = "UPDATE parkingSpot SET status = 'Occupied' "
                + "WHERE spotId = ? AND status = 'Available'";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, spotId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean hasValidReservationForSpotNow(String plate, String spotId) {
        String sql
                = "SELECT COUNT(*) "
                + "FROM reservation "
                + "WHERE UPPER(TRIM(plate)) = ? "
                + "  AND spotId = ? "
                + "  AND UPPER(status) = 'ACTIVE' "
                + "  AND NOW() <= endTime";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, plate.trim().toUpperCase());
            ps.setString(2, spotId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<Ticket> getCompletedTickets() {
    List<Ticket> list = new ArrayList<>();
    String sql = "SELECT t.*, v.vehicleType FROM ticket t " +
                 "JOIN vehicle v ON t.licensePlate = v.licensePlate " +
                 "WHERE t.exitTime IS NOT NULL";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        VehicleFactory factory = new VehicleFactory();

        while (rs.next()) {
            //Recreate Vehicle object
            String plate = rs.getString("licensePlate");
            String type = rs.getString("vehicleType");
            double debt = rs.getDouble("carriedOverFine");
            Vehicle v = factory.createVehicle(type, plate, debt);

            //Recreate Spot object 
            ParkingSpot s = new RegularSpot(rs.getString("spotId"), 1);

            Ticket t = new Ticket(
                rs.getString("ticketId"),
                v,
                s,
                rs.getTimestamp("entryTime").toLocalDateTime(),
                rs.getString("fineScheme"),
                debt
            );

            t.closeTicket(
                rs.getTimestamp("exitTime").toLocalDateTime(),
                rs.getDouble("parkingFee"),
                rs.getDouble("fineAmount"),
                rs.getDouble("totalPaid"),
                rs.getString("paymentMethod")
            );

            list.add(t);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}
} 


