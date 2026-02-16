/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
public abstract class ParkingSpot {
    private String spotId;
    private SpotType spotType;
    private SpotStatus status;
    private double hourlyRate;
    private int floorNumber;
    private Vehicle currentVehicle;    
    
    public ParkingSpot(String spotId, int floorNumber, SpotType spotType, double hourlyRate)
    {
        this.spotId = spotId;
        this.floorNumber = floorNumber;
        this.spotType = spotType;
        this.hourlyRate = hourlyRate;
        this.status = SpotStatus.AVAILABLE;
        this.currentVehicle = null;
    }
    
    public SpotStatus getStatus(){
        return status;
    }
    
    public void setStatus(SpotStatus status) {
        this.status = status;
    }
    
    public boolean isAvailable()
    {
        return status == SpotStatus.AVAILABLE;
    }
    
    public boolean isOccupied() {
        return status == SpotStatus.OCCUPIED;
    }
    
    public void parkVehicle(Vehicle v)
    {
        if (v == null || !isAvailable() || !canParkVehicle(v))
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
    
    public double getHourlyRate()
    {
        return hourlyRate;
    }
    
    public SpotType getSpotType()
    {
        return spotType;
    }
    
    public int getFloorNumber()
    {
        return floorNumber;
    }
    
    public Vehicle getCurrentVehicle()
    {
        return currentVehicle;
    }
    
        public abstract boolean canParkVehicle(Vehicle vehicle);
}
