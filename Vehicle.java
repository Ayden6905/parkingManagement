/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */

import java.time.LocalDateTime;

public abstract class Vehicle {
    private String licensePlate;
    private VehicleType vehicleType;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime; //null if still in parking
    
    protected Vehicle(String licensePlate, VehicleType vehicleType)
    {
        this.licensePlate = licensePlate;
        this.vehicleType= vehicleType;
        this.entryTime = LocalDateTime.now();
        this.exitTime = null;
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
    
    public void setExitTime(LocalDateTime exitTime)
    {
        this.exitTime = exitTime;
    }
}
