/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;
/**
 *
 * @author HP
 */

import java.time.LocalDateTime;

public abstract class Vehicle {

    private final String licensePlate;
    private final VehicleType vehicleType;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private double outstandingFines;
    
    //newly added
    private boolean handicappedCardHolder;

    protected Vehicle(String licensePlate, VehicleType vehicleType, double outstandingFines) {
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.entryTime = LocalDateTime.now();
        this.exitTime = null;
        this.outstandingFines = outstandingFines;
    }
    
    public String getLicensePlate()
    {
        return licensePlate;
    }
    
    public VehicleType getVehicleType()
    {
        return vehicleType;
    }
    
    public LocalDateTime getEntryTime()
    {
        return entryTime;
    }
    
    public LocalDateTime getExitTime()
    {
        return exitTime;
    }
    
    public double getOutstandingFines() {
        return outstandingFines;
    }
    
    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public void setOutstandingFines(double amount) {
        this.outstandingFines = amount;
    }
    
    public void addFine(double amount) {
        if (amount > 0) {
            this.outstandingFines += amount;
        }
    }        
    
    public boolean isHandicappedCardHolder()
    {
        return handicappedCardHolder;
    }
    
    public void setHandicappedCardHolder (boolean handicappedCardHolder)
    {
        this.handicappedCardHolder = handicappedCardHolder;
    }
}