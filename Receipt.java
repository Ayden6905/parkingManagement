/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author NurqistinaAtashah
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Receipt {
    private int receiptId;
    private Ticket ticket;
    private String plate;
    private int hours;
    private double parkingFee;
    private double fineAmount;
    private double totalPaid;
    private LocalDateTime issuedTime;

    public Receipt(int receiptId, Ticket ticket, double parkingFee,
                   double fineAmount, double totalPaid, LocalDateTime issuedTime) {
        this.receiptId = receiptId;
        this.ticket = ticket;
        this.parkingFee = parkingFee;
        this.fineAmount = fineAmount;
        this.totalPaid = totalPaid;
        this.issuedTime = issuedTime;
        this.plate = ticket != null ? ticket.getLicensePlate().getLicensePlate() : null;
        this.hours = ticket != null ? ticket.getTotalHours() : 0;
    }
    
    public Receipt(String plate, int hours, double total) {
        this.plate = plate;
        this.hours = hours;
        this.totalPaid = total;
        this.parkingFee = total;
        this.fineAmount = 0.0;
        this.issuedTime = LocalDateTime.now();
    }
    
    public Receipt(Ticket ticket, double parkingFee, double fineAmount, double totalPaid) {
        this.ticket = ticket;
        this.parkingFee = parkingFee;
        this.fineAmount = fineAmount;
        this.totalPaid = totalPaid;
        this.issuedTime = LocalDateTime.now();
        this.plate = ticket != null ? ticket.getLicensePlate().getLicensePlate() : null;
        this.hours = ticket != null ? ticket.getTotalHours() : 0;
    }
    
    public void generateReceipt() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        System.out.println("--- PARKING RECEIPT ---");
        System.out.println("Receipt ID: " + receiptId);
        if (ticket != null) {
            System.out.println("Ticket ID: " + ticket.getTicketId());
            System.out.println("Plate: " + ticket.getLicensePlate().getLicensePlate());
            System.out.println("Entry Time: " + ticket.getEntryTime().format(fmt));
            System.out.println("Exit Time: " + (ticket.getExitTime() != null ? ticket.getExitTime().format(fmt) : "-"));
            System.out.println("Duration: " + ticket.getTotalHours() + " hour(s)");
            System.out.println("Payment Method: " + ticket.getPaymentMethod());
        } else {
            System.out.println("Plate: " + plate);
            System.out.println("Duration: " + hours + " hour(s)");
        }

        System.out.println("Parking Fee: RM " + String.format("%.2f", parkingFee));
        System.out.println("Fine: RM " + String.format("%.2f", fineAmount));
        System.out.println("Total Paid: RM " + String.format("%.2f", totalPaid));
        System.out.println("Issued Time: " + issuedTime.format(fmt));
        System.out.println("-------------------------");
    }
    
    @Override
    public String toString() {
        if (ticket != null) {
            return "--- PARKING RECEIPT ---\n" +
                   "Plate: " + ticket.getLicensePlate().getLicensePlate() + "\n" +
                   "Hours: " + ticket.getTotalHours() + "\n" +
                   "Total Paid: RM " + String.format("%.2f", totalPaid);
        } else {
            return "--- PARKING RECEIPT ---\n" +
                   "Plate: " + plate + "\n" +
                   "Hours: " + hours + "\n" +
                   "Total Paid: RM " + String.format("%.2f", totalPaid);
        }
    }
    
    public int getReceiptId() { return receiptId; }
    public Ticket getTicket() { return ticket; }
    public double getParkingFee() { return parkingFee; }
    public double getFineAmount() { return fineAmount; }
    public double getTotalPaid() { return totalPaid; }
    public LocalDateTime getIssuedTime() { return issuedTime; }
}