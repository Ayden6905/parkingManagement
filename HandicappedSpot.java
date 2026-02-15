/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
public class HandicappedSpot extends ParkingSpot {
    public HandicappedSpot(String spotId, int floorNumber)
    {
        super(spotId, floorNumber, SpotType.HANDICAPPED, 2.0);
    }
    
    @Override
    public boolean canParkVehicle(Vehicle v)
    {
        return v.getVehicleType() == VehicleType.HANDICAPPED || v.isHandicappedCardHolder();
    }
}
