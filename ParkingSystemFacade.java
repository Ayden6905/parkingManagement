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

public class ParkingSystemFacade {
    private ParkingLot parkingLot;
    private FineManager fineManager;
    private TicketService ticketService;

    public ParkingSystemFacade() {
        this.parkingLot = new ParkingLot(5); 
        this.fineManager = new FineManager();
        this.ticketService = new TicketService();
    }
    
    public boolean checkDatabaseConnection() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
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
    
    public String handleVehicleEntry(String plate, String vehicleType, String spotId) {
        if (Ticket.findActiveByPlate(plate) != null) {
            return "Error: Vehice with plate" + plate + " is already inside the parking lot.";
        }
        
        ticketService.createTicket(plate, vehicleType, spotId);

        Ticket ticket = Ticket.findActiveByPlate(plate);

        if (ticket != null) {
            return ticket.generateFormattedTicket();
        }

        return "Error: Failed to generate ticket record.";
    }
    
    public Receipt handleVehicleExit(String plate) {
        int hoursParked = 2;
        double hourlyRate = 3.00;
        double fine = fineManager.getOutstandingFineByPlate("NONE");
        double totalPaid = (hoursParked * hourlyRate) + fine;
        return ticketService.closeTicketAndPay(plate, hourlyRate, fine, "Cash");
    }
    
    public void changeSystemFineScheme(String schemeName) {
        System.out.println("Switching scheme to: " + schemeName);
    }
    
    public String createTicket(String plate, String vehicleType, String spotId) {
        return ticketService.createTicket(plate, vehicleType, spotId);
    }
    
    public ParkingSummary getParkingSummary(String plate, double hourlyRate) {
        Ticket ticket = Ticket.findActiveByPlate(plate);
        if (ticket == null) return null;
        
        double fineOwed = fineManager.getOutstandingFineByPlate(plate);        
        
        int duration = ticket.calculateDurationHours();
        double parkingFee = duration * hourlyRate;
        double totalPayment = parkingFee + fineOwed;

        return new ParkingSummary(
                ticket.getTicketId(),
                ticket.getLicensePlate(),
                ticket.getEntryTime(),
                LocalDateTime.now(),
                duration,
                parkingFee,
                fineOwed,
                totalPayment
        );
    }
    
    public java.util.List<String> getAvailableSpots() {
        java.util.List<String> spots = new java.util.ArrayList<>();
        
        String sql = "SELECT spotId FROM parkingSpot WHERE status='Available'";
        
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                spots.add(rs.getString("spotId"));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching spots: " + e.getMessage());
        }

        return spots;
    }    
    
    public Receipt processPayment(String plate, double hourlyRate, double fineToPay, String method) {
                System.out.println("Saving receipt to database...");
        return ticketService.closeTicketAndPay(plate, hourlyRate, fineToPay, method);
    }
    
    public List<RevenueRecord> getRevenueReport() {
        return ticketService.getRevenueReport();
    }
}