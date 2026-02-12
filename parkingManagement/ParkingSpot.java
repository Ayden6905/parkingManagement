/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author HP
 */
public abstract class ParkingSpot {
    protected String spotId;
    protected SpotType spotType;
    protected SpotStatus status;
    protected double hourlyRate;
    protected int floorNumber;
    protected Vehicle currentVehicle;
    
    public ParkingSpot(String spotId, int floorNumber)
    {
        this.spotId = spotId;
        this.floorNumber = floorNumber;
        this.status = SpotStatus.AVAILABLE;
        this.currentVehicle = null;
    }
    
    public boolean isAvailable()
    {
        return status == SpotStatus.AVAILABLE;
    }
    
    public void parkVehicle(Vehicle v)
    {
        if (v == null)
            return;
        
        if (!isAvailable())
            return;
        
        if (!canParkVehicle(v))
            return;
        
        this.currentVehicle = v;
        this.status = SpotStatus.OCCUPIED;
    }
    
    public void removeVehicle()
    {
        this.currentVehicle = null;
        this.status = SpotStatus.AVAILABLE;
    }
    
    public String getSpotId()
    {
        return spotId;
    }
    
    public double getHourlyRte()
    {
        return hourlyRate;
    }
    
    public SpotType getSpotType()
    {
        return spotType;
    }
    
    protected abstract boolean canParkVehicle(Vehicle v);
}
