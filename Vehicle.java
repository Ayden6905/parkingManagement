/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingmanagement;

/**
 *
 * @author ayden
 */
import java.time.LocalDateTime;

public abstract class Vehicle {
    protected String licensePlate;
    protected LocalDateTime entryTime;
    protected LocalDateTime exitTime;
    protected double outstandingFines;

    public Vehicle(String licensePlate) {
        this.licensePlate = licensePlate;
        this.entryTime = LocalDateTime.now(); // Sets entry time to 'now'
        this.outstandingFines = 0.0;
    }

    // Abstract method: Forces subclasses to identify themselves
    public abstract VehicleType getVehicleType();

    // --- Getters and Setters ---
    public String getLicensePlate() { return licensePlate; }
    
    public LocalDateTime getEntryTime() { return entryTime; }
    
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    
    public double getOutstandingFines() { return outstandingFines; }
    public void addFine(double amount) { this.outstandingFines += amount; }
}
