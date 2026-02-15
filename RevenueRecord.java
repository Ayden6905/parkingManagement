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

public class RevenueRecord {
    
    private String licensePlate;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private double totalPaid;
    private String paymentMethod;
    private LocalDateTime receiptTime;

    public RevenueRecord(String licensePlate,
                         LocalDateTime entryTime,
                         LocalDateTime exitTime,
                         double totalPaid,
                         String paymentMethod,
                         LocalDateTime receiptTime) {

        this.licensePlate = licensePlate;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.totalPaid = totalPaid;
        this.paymentMethod = paymentMethod;
        this.receiptTime = receiptTime;
    }
    
    public String getLicensePlate() { return licensePlate; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public double getTotalPaid() { return totalPaid; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getReceiptTime() { return receiptTime; }
}
