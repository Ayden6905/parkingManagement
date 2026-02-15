/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parkingmanagement;
/**
 *
 * @author User
 */
import java.time.LocalDateTime;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketService {
    
    private final PaymentService paymentService;
    private final VehicleFactory vehicleFactory;
    
    public TicketService() {
        this.paymentService = new PaymentService();
        this.vehicleFactory = new VehicleFactory();
    }
    
    public String createTicket(String plate, String vehicleType, String spotId) {
        Vehicle vehicle = vehicleFactory.createVehicle(vehicleType, plate);
        
        ParkingSpot spot = ParkingLot.getInstance().findSpotById(spotId);
        if (spot == null) {
            throw new RuntimeException("Spot not found: " + spotId);
        }
        
        String ticketId = "T-" + plate + "-" + System.currentTimeMillis();
        Ticket ticket = new Ticket(ticketId, vehicle, spot, LocalDateTime.now());
        ticket.saveEntry();

        return ticketId;
    }
    
    public Receipt closeTicketAndPay(String plate,
                                 double hourlyRate,
                                 double fineToPay,
                                 String paymentMethod) {

        Ticket ticket = Ticket.findActiveByPlate(plate);
        if (ticket == null) return null;

        LocalDateTime exitTime = LocalDateTime.now();
        int hours = ticket.calculateDurationHours();
        double parkingFee = hours * hourlyRate;
        double totalPaid = parkingFee + fineToPay;
        double remainingBalance = 0.0; 

        ticket.closeTicket(exitTime, parkingFee, fineToPay, totalPaid, paymentMethod);

        insertPayment(ticket.getTicketId(), totalPaid, paymentMethod);
        insertReceipt(ticket.getTicketId(), parkingFee, fineToPay, totalPaid);

        freeParkingSpot(ticket.getSpotId().getSpotId());

        return new Receipt(ticket, parkingFee, fineToPay, totalPaid, paymentMethod);
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
    
    private void insertPayment(String ticketId, double amount, String method) {

        String sql = "INSERT INTO payment (ticketId, amount, paymentMethod) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ticketId);
            ps.setDouble(2, amount);
            ps.setString(3, method);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Payment insert error: " + e.getMessage());
        }
    }

    private void insertReceipt(String ticketId,
                               double parkingFee,
                               double fineAmount,
                               double totalPaid) {

        String sql = "INSERT INTO receipt (ticketId, parkingFee, fineAmount, totalPaid) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ticketId);
            ps.setDouble(2, parkingFee);
            ps.setDouble(3, fineAmount);
            ps.setDouble(4, totalPaid);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Receipt insert error: " + e.getMessage());
        }
    }

    private void freeParkingSpot(String spotId) {

        String sql = "UPDATE parkingSpot SET status='Available' WHERE spotId=?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, spotId);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Spot free error: " + e.getMessage());
        }
    }
}