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
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class ParkingSystemFacade {

    private final FineManager fineManager;
    private final TicketService ticketService;
    private VehicleFactory vehicleFactory = new VehicleFactory();

    public ParkingSystemFacade() {
        this.fineManager = new FineManager();
        this.ticketService = new TicketService();

        String savedScheme = getCurrentFineScheme();
        this.fineManager.setStrategy(savedScheme);
        System.out.println("System loaded with Fine Scheme: " + savedScheme);
    }

    //db check
    public boolean checkDatabaseConnection() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //admin login
    public boolean authenticateAdmin(String username, String password) {

        String query = "SELECT * FROM admin WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.out.println("Login Error: " + e.getMessage());
            return false;
        }
    }

    //vehicle entry
    public String handleVehicleEntry(String plate, String vehicleType, String spotId, boolean isHandicappedCardHolder) {

        if (plate == null || plate.trim().isEmpty()) {
            return "Error: License plate required.";
        }

        if (Ticket.findActiveByPlate(plate) != null) {
            return "Error: Vehicle with plate " + plate + " is already inside.";
        }

        try {
            ticketService.createTicket(plate, vehicleType, spotId, isHandicappedCardHolder);

            Ticket ticket = Ticket.findActiveByPlate(plate);

            if (ticket != null) {
                return ticket.generateFormattedTicket();
            }

        } catch (Exception e) {
            return "Error during vehicle entry: " + e.getMessage();
        }

        return "Error: Failed to generate ticket.";
    }

    //vehicle exit
    public Receipt handleVehicleExit(String plate) {

        if (plate == null || plate.trim().isEmpty()) {
            return null;
        }

        Ticket ticket = Ticket.findActiveByPlate(plate);
        if (ticket == null) return null;

        int hoursParked = ticket.calculateDurationHours();
        double hourlyRate = 3.00;

        double fine = fineManager.calculateFine(hoursParked);

        return ticketService.closeTicketAndPay(
                plate,
                hourlyRate,
                fine,
                "Cash"
        );
    }

    //to change fine scheme
    public boolean changeSystemFineSchemeDb(String schemeName) {

        String sql = "UPDATE fineStrategy SET current_scheme = ? WHERE id = 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, schemeName);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                fineManager.setStrategy(schemeName);
                System.out.println("Strategy switched to: " + schemeName);
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error updating fine scheme: " + e.getMessage());
        }

        return false;
    }

    public String getCurrentFineScheme() {

        String sql = "SELECT current_scheme FROM fineStrategy WHERE id = 1";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getString("current_scheme");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Fixed"; // default
    }

    //parking summary
    public ParkingSummary getParkingSummary(String plate, double hourlyRate) {

        Ticket ticket = Ticket.findActiveByPlate(plate);
        if (ticket == null) return null;

        int duration = ticket.calculateDurationHours();
        double fineOwed = fineManager.calculateFine(duration);
        double parkingFee = duration * hourlyRate;
        double totalPayment = parkingFee + fineOwed;

        return new ParkingSummary(
                ticket.getTicketId(),
                ticket.getLicensePlate().getLicensePlate(),
                ticket.getEntryTime(),
                LocalDateTime.now(),
                duration,
                parkingFee,
                fineOwed,
                totalPayment
        );
    }

    //available spots
    public List<String> getAvailableSpotsFor(String plate, String vehicleType, boolean cardHolder) {
        Vehicle v = vehicleFactory.createVehicle(vehicleType, "TEMP");
        v.setHandicappedCardHolder(cardHolder);

        List<String> ids = new ArrayList<>();
        for (ParkingSpot s : ParkingLot.getInstance().getAvailableSpots(v, plate)) {
            ids.add(s.getSpotId());
        }
        return ids;
    }

    //payment processing
    public Receipt processPayment(String plate,
                                  double hourlyRate,
                                  double fineToPay,
                                  String method) {

        return ticketService.closeTicketAndPay(
                plate,
                hourlyRate,
                fineToPay,
                method
        );
    }

    //revenue report
    public List<RevenueRecord> getRevenueReport() {
        return ticketService.getRevenueReport();
    }
    
    //newly added
    public double calculateParkingFee(Vehicle v, ParkingSpot spot, int hours) {

        // for handicapped driver
        if (v.isHandicappedCardHolder()) {
            
            if (spot.getSpotType() == SpotType.HANDICAPPED) {
                return 0.0;
            }
            
            if (spot.getSpotType() == SpotType.REGULAR) {
                return 2.0;                
            }
        }
        return hours * spot.getHourlyRate();
    }
}
