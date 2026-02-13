/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author User
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class TicketService {
    
    private PaymentService paymentService;
    
    public TicketService() {
        this.paymentService = new PaymentService();
    }
    
    public String createTicket(String plate, String spotId) {
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
}
