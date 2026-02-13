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
    private String ticketId;
    private String plate;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private int hours;
    private double parkingFee;
    private double fineAmount;
    private double totalPaid;
    private String paymentMethod;

    public Receipt(String ticketId, String plate, LocalDateTime entryTime,
            LocalDateTime exitTime, int hours, double parkingFee, double fineAmount,
            double totalPaid, String paymentMethod) {
        this.ticketId = ticketId;
        this.plate = plate;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.hours = hours;
        this.parkingFee = parkingFee;
        this.fineAmount = fineAmount;
        this.totalPaid = totalPaid;
        this.paymentMethod = paymentMethod;
    }
    
    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        
        return "--- PARKING RECEIPT ---\n" +
               "Ticket ID: " + ticketId + "\n" +
               "Plate: " + plate + "\n" +
               "Entry Time: " + entryTime.format(fmt) + "\n" +
               "Exit Time: " + exitTime.format(fmt) + "\n" +
               "Duration: " + hours + " hour(s)\n" +
               "Parking Fee: RM " + String.format("%.2f", parkingFee) + "\n" +
               "Fine: RM " + String.format("%.2f", fineAmount) + "\n" +
               "Total Paid: RM " + String.format("%.2f", totalPaid) + "\n" +
               "Payment Method: " + paymentMethod;
    }
}