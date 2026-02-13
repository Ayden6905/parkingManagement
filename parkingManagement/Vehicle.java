/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author HP
 */
public abstract class Vehicle {
    protected String licensePlate;
    protected VehicleType vehicleType;
    
    public Vehicle(String licensePlate, VehicleType vehicleType)
    {
        this.licensePlate = licensePlate;
        this.vehicleType= vehicleType;
    }
    
    public String getLicensePlate()
    {
        return licensePlate;
    }
    
    public VehicleType getVehicleType()
    {
        return vehicleType;
    }
}
