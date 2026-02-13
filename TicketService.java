/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author User
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketService {
    
    private PaymentService paymentService;
    
    public TicketService() {
        this.paymentService = new PaymentService();
    }
    
    public String createTicket(String plate, String vehicleType, String spotId) {
        String vehicleSQL = "INSERT IGNORE INTO vehicle (licensePlate, vehicleType) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(vehicleSQL)) {

            ps.setString(1, plate);
            ps.setString(2, "Car");
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Vehicle insert error: " + e.getMessage());
        }
        
        String ticketId = "T-" + plate + "-" + System.currentTimeMillis();
        Ticket ticket = new Ticket(ticketId, plate, spotId, LocalDateTime.now());
        ticket.saveEntry();
        return ticketId;
    }
    
    public Receipt closeTicketAndPay(String plate, double hourlyRate,
                                      double fineToPay, String paymentMethod) {

        Ticket ticket = Ticket.findActiveByPlate(plate);
        if (ticket == null) {
            return null;
        }

        LocalDateTime exitTime = LocalDateTime.now();
        int hours = ticket.calculateDurationHours();
        double parkingFee = hours * hourlyRate;
        double totalPaid = parkingFee + fineToPay;

        ticket.closeTicket(exitTime, parkingFee, fineToPay, totalPaid, paymentMethod);
        
        String paymentSQL = "INSERT INTO payment (ticketId, amount, paymentMethod) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(paymentSQL)) {

            ps.setString(1, ticket.getTicketId());
            ps.setDouble(2, totalPaid);
            ps.setString(3, paymentMethod);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Payment insert error: " + e.getMessage());
        }
        
        String receiptSQL = "INSERT INTO receipt (ticketId, parkingFee, fineAmount, totalPaid) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(receiptSQL)) {

            ps.setString(1, ticket.getTicketId());
            ps.setDouble(2, parkingFee);
            ps.setDouble(3, fineToPay);
            ps.setDouble(4, totalPaid);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Receipt insert error: " + e.getMessage());
        }
        
         String freeSpotSQL = "UPDATE parkingSpot SET status='Available' WHERE spotId=?";
         try (Connection conn = DatabaseConfig.getConnection();
              PreparedStatement ps = conn.prepareStatement(freeSpotSQL)) {

            ps.setString(1, ticket.getSpotId());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Spot free error: " + e.getMessage());
        }

        return new Receipt(
                ticket.getTicketId(),
                ticket.getLicensePlate(),
                ticket.getEntryTime(),
                LocalDateTime.now(),
                hours,
                parkingFee,
                fineToPay,
                totalPaid,
                paymentMethod
        );
    }
    
    public List<RevenueRecord> getRevenueReport() {
        
        List<RevenueRecord> records = new ArrayList<>();
        
        String sql = """
            SELECT t.licensePlate, t.entryTime, t.exitTime,
                           t.totalPaid, t.paymentMethod, r.issuedTime
                    FROM ticket t
                    JOIN receipt r ON t.ticketId = r.ticketId
                    ORDER BY r.issuedTime DESC
        """;
            
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                records.add(new RevenueRecord(
                        rs.getString("licensePlate"),
                        rs.getTimestamp("entryTime").toLocalDateTime(),
                        rs.getTimestamp("exitTime").toLocalDateTime(),
                        rs.getDouble("totalPaid"),
                        rs.getString("paymentMethod"),
                        rs.getTimestamp("issuedTime").toLocalDateTime()
                ));
            }
            
        } catch (SQLException e) {
            System.out.println("Revenue report error: " + e.getMessage());
        }
            
        return records;                    
    }
}
