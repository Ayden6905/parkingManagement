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
    protected boolean canParkVehicle(Vehicle v)
    {
        return (v instanceof Car) || (v instanceof SUV) || (v instanceof HandicappedVehicle);
    }
}
