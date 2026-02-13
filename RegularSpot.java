/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
public class RegularSpot extends ParkingSpot {
    public RegularSpot(String spotId, int floorNumber) {
        super(spotId, floorNumber, SpotType.REGULAR, 5.0);
    }

    @Override
    protected boolean canParkVehicle(Vehicle v) {
        return (v instanceof Car) || (v instanceof SUV) || (v instanceof HandicappedVehicle);
    }
}
