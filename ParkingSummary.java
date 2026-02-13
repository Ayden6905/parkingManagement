/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author User
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParkingSummary {
    private String ticketId;
    private String plate;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private int hours;
    private double parkingFee;
    private double fineAmount;
    private double totalPayment;
    
    public ParkingSummary(String ticketId, String plate, LocalDateTime entryTime, LocalDateTime exitTime,
                          int hours, double parkingFee, double fineAmount, double totalPayment) {
        this.ticketId = ticketId;
        this.plate = plate;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.hours = hours;
        this.parkingFee = parkingFee;
        this.fineAmount = fineAmount;
        this.totalPayment = totalPayment;
    }
    
    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "--- PARKING SUMMARY ---\n" +
               "Ticket ID: " + ticketId + "\n" +
               "Plate: " + plate + "\n" +
               "Entry Time: " + entryTime.format(fmt) + "\n" +
               "Exit Time: " + exitTime.format(fmt) + "\n" +
               "Duration: " + hours + " hour(s)\n" +
               "Parking Fee: RM " + String.format("%.2f", parkingFee) + "\n" +
               "Fine: RM " + String.format("%.2f", fineAmount) + "\n" +
               "Total Payment: RM " + String.format("%.2f", totalPayment);
    }
    
    //getters
    public String getTicketId() { return ticketId; }
    public String getPlate() { return plate; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public int getHours() { return hours; }
    public double getParkingFee() { return parkingFee; }
    public double getFineAmount() { return fineAmount; }
    public double getTotalPayment() { return totalPayment; }    
}
