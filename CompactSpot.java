/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author HP
 */
public class CompactSpot extends ParkingSpot {
    public CompactSpot(String spotId, int floorNumber)
    {
        super(spotId, floorNumber, SpotType.COMPACT, 2.0);
    }
    
    @Override
    protected boolean canParkVehicle(Vehicle v)
    {
        VehicleType t = v.getVehicleType();
        return t == VehicleType.MOTORCYCLE || t == VehicleType.CAR;
    }
}
