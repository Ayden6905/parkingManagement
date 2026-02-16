/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
public class ReservedSpot extends ParkingSpot {
    public ReservedSpot(String spotId, int floorNumber)
    {
        super(spotId, floorNumber, SpotType.RESERVED, 10.0);
    }
    
    @Override
    public boolean canParkVehicle(Vehicle v)
    {
        VehicleType t = v.getVehicleType();
        return t == VehicleType.CAR || t == VehicleType.SUV || t == VehicleType.HANDICAPPED;
    }
}
