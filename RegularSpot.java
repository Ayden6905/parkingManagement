/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author HP
 */
public class RegularSpot extends ParkingSpot {
    public RegularSpot(String spotId, int floorNumber)
    {
        super(spotId, floorNumber, SpotType.REGULAR, 5.0);
    }
    
    @Override
    protected boolean canParkVehicle(Vehicle v)
    {
        VehicleType t = v.getVehicleType();
        return t == VehicleType.CAR || t== VehicleType.SUV || t == VehicleType.HANDICAPPED;
    }
}
