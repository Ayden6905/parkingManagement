/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author ayden
 */
public class HandicappedVehicle extends Vehicle {
    public HandicappedVehicle(String licensePlate, double outstandingFines) {
        super(licensePlate, VehicleType.HANDICAPPED,outstandingFines);
    }

    @Override
    public VehicleType getVehicleType() {
        return VehicleType.HANDICAPPED;
    }
}