/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parking_management;

/**
 *
 * @author ayden
 */
public class HandicappedVehicle extends Vehicle {
    public HandicappedVehicle(String licensePlate) {
        super(licensePlate);
    }

    @Override
    public VehicleType getVehicleType() {
        return VehicleType.HANDICAPPED;
    }
}