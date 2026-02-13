/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author ayden
 */
public class SUV extends Vehicle {
    public SUV(String licensePlate) {
        super(licensePlate, VehicleType.SUV);
    }

    @Override
    public VehicleType getVehicleType() {
        return VehicleType.SUV;
    }
}
