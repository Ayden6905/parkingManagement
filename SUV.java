/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parkingmanagement;

/**
 *ssss
 * @author ayden
 */
public class SUV extends Vehicle {
    public SUV(String licensePlate) {
        super(licensePlate);
    }

    @Override
    public VehicleType getVehicleType() {
        return VehicleType.SUV;
    }
}
