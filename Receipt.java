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
    private int durationHours;
    private double parkingFee;
    private double fineAmount;
    private double totalPaid;
    private String paymentMethod;
    private double remainingBalance;
    private LocalDateTime issuedTime;

    public Receipt(int receiptId, Ticket ticket, double parkingFee,
                   double fineAmount, double totalPaid, String paymentMethod,
                   double remainingBalance, LocalDateTime issuedTime) {
        this.receiptId = receiptId;
        this.ticket = ticket;
        this.parkingFee = parkingFee;
        this.fineAmount = fineAmount;
        this.totalPaid = totalPaid;
        this.paymentMethod = paymentMethod;
        this.remainingBalance = remainingBalance;
        this.issuedTime = issuedTime != null ? issuedTime : LocalDateTime.now();

        if (ticket != null) {
            this.plate = ticket.getLicensePlate().getLicensePlate();
            this.durationHours = ticket.calculateDurationHours();
        } else {
            this.plate = null;
            this.durationHours = 0;
        }
    }
    
    public Receipt(Ticket ticket, double parkingFee, double fineAmount,
                   double totalPaid, String paymentMethod, double remainingBalance) {
        this.ticket = ticket;
        this.parkingFee = parkingFee;
        this.fineAmount = fineAmount;
        this.totalPaid = totalPaid;
        this.paymentMethod = paymentMethod;
        this.remainingBalance = remainingBalance;
        this.issuedTime = LocalDateTime.now();

        if (ticket != null) {
            this.plate = ticket.getLicensePlate().getLicensePlate();
            this.durationHours = ticket.calculateDurationHours();
        } else {
            this.plate = null;
            this.durationHours = 0;
        }
    }
    
    public void generateReceipt() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        System.out.println("--- PARKING RECEIPT ---");
        if (ticket != null) {
            System.out.println("Ticket ID: " + ticket.getTicketId());
            System.out.println("Plate: " + ticket.getLicensePlate().getLicensePlate());
            System.out.println("Entry Time: " + ticket.getEntryTime().format(fmt));
            System.out.println("Exit Time: " + (ticket.getExitTime() != null ? ticket.getExitTime().format(fmt) : "-"));
            System.out.println("Duration: " + ticket.calculateDurationHours() + " hour(s)");
            System.out.println("Payment Method: " + ticket.getPaymentMethod());
        } else {
            System.out.println("Plate: " + plate);
            System.out.println("Duration: " + durationHours + " hour(s)");
        }

        System.out.println("Parking Fee: RM " + String.format("%.2f", parkingFee));
        System.out.println("Fine: RM " + String.format("%.2f", fineAmount));
        System.out.println("Total Paid: RM " + String.format("%.2f", totalPaid));
        System.out.println("Remaining Balance: RM " + String.format("%.2f", remainingBalance));
        System.out.println("Issued Time: " + issuedTime.format(fmt));
        System.out.println("-------------------------");
    }

    @Override
    public String toString() {
        return "--- PARKING RECEIPT ---\n" +
               "Plate: " + plate + "\n" +
               "Duration: " + durationHours + " hour(s)\n" +
               "Parking Fee: RM " + String.format("%.2f", parkingFee) + "\n" +
               "Fine: RM " + String.format("%.2f", fineAmount) + "\n" +
               "Total Paid: RM " + String.format("%.2f", totalPaid) + "\n" +
               "Remaining Balance: RM " + String.format("%.2f", remainingBalance);
    }
    
    public int getReceiptId() { return receiptId; }
    public Ticket getTicket() { return ticket; }
    public String getPlate() { return plate; }
    public int getDurationHours() { return durationHours; }
    public double getParkingFee() { return parkingFee; }
    public double getFineAmount() { return fineAmount; }
    public double getTotalPaid() { return totalPaid; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getRemainingBalance() { return remainingBalance; }
    public LocalDateTime getIssuedTime() { return issuedTime; }
}