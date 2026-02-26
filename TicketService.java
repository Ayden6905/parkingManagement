/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

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

    public String createTicket(String plate, String vehicleType, String spotId, boolean isHandicapped, String scheme, double carriedOverFine) {
        //Create vehicle object
        Vehicle vehicle = vehicleFactory.createVehicle(vehicleType, plate);
        vehicle.setHandicappedCardHolder(isHandicapped);
        
        //Find the parking spot
        ParkingSpot spot = ParkingLot.getInstance().findSpotById(spotId);
        if (spot == null) {
            throw new RuntimeException("Spot not found in system: " + spotId);
        }

        //Generate unique Ticket ID
        String ticketId = "T-" + plate + "-" + System.currentTimeMillis();

        //Instantiate Ticket with debt tracking
        Ticket ticket = new Ticket(ticketId, vehicle, spot, LocalDateTime.now(), scheme, carriedOverFine);

        //LOCK spot in DB (prevents 2 cars taking same spot)
        ParkingRepository repo = new ParkingRepository();
        boolean ok = repo.occupySpot(spotId);
        if (!ok) {
            throw new RuntimeException("Spot already taken. Please choose another spot.");
        }

        //save ticket
        ticket.saveEntry();

        return ticketId;
    }

    public String createTicket(String plate, String vehicleType, String spotId, boolean isHandicappedCardHolder) {
        return createTicket(plate, vehicleType, spotId, isHandicappedCardHolder, "Fixed", 0.0);
    }

    public void closeTicket(String plate) {
        String sql = "UPDATE ticket SET exitTime = ? WHERE licensePlate = ? AND exitTime IS NULL";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, plate);
            ps.executeUpdate();

            Ticket t = Ticket.findActiveByPlate(plate);
            if (t != null) {
                freeParkingSpot(t.getSpotId().getSpotId());
            }

        } catch (SQLException e) {
            System.out.println("Error closing ticket: " + e.getMessage());
        }
    }

    public Ticket getActiveTicket(String plate) {
        return Ticket.findActiveByPlate(plate);
    }

    public Receipt closeTicketAndPay(String plate, double hourlyRate, double fineToPay, String paymentMethod) {
        Ticket ticket = Ticket.findActiveByPlate(plate);
        if (ticket == null) return null;

        LocalDateTime exitTime = LocalDateTime.now();
        int hours = ticket.calculateDurationHours();
        double parkingFee = hours * hourlyRate;
        double totalPaid = parkingFee + fineToPay;

        ticket.closeTicket(exitTime, parkingFee, fineToPay, totalPaid, paymentMethod);

        insertPayment(ticket.getTicketId(), totalPaid, paymentMethod);
        insertReceipt(ticket.getTicketId(), parkingFee, fineToPay, totalPaid);
        freeParkingSpot(ticket.getSpotId().getSpotId());

        return new Receipt(ticket, parkingFee, fineToPay, totalPaid, paymentMethod);
    }

    public List<RevenueRecord> getRevenueReport() {
        List<RevenueRecord> records = new ArrayList<>();
        String sql = "SELECT t.licensePlate, t.entryTime, t.exitTime, t.totalPaid, t.paymentMethod, r.issuedTime " +
                     "FROM ticket t JOIN receipt r ON t.ticketId = r.ticketId ORDER BY r.issuedTime DESC";

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

    private void updateSpotStatus(String spotId, String status) {
        String sql = "UPDATE parkingSpot SET status = ? WHERE spotId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, spotId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void insertPayment(String ticketId, double amount, String method) {
        String sql = "INSERT INTO payment (ticketId, amount, paymentMethod) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticketId);
            ps.setDouble(2, amount);
            ps.setString(3, method);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void insertReceipt(String ticketId, double parkingFee, double fineAmount, double totalPaid) {
        String sql = "INSERT INTO receipt (ticketId, parkingFee, fineAmount, totalPaid) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticketId);
            ps.setDouble(2, parkingFee);
            ps.setDouble(3, fineAmount);
            ps.setDouble(4, totalPaid);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void freeParkingSpot(String spotId) {
        String sql = "UPDATE parkingSpot SET status='Available' WHERE spotId=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, spotId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }        
}