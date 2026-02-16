/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parkingmanagement;
/**
 *
 * @author ayden
 */
public class Car extends Vehicle {
    
    // Updated constructor to accept fines
    public Car(String licensePlate, double fines) {
        // Calls the super constructor in Vehicle.java
        super(licensePlate, VehicleType.CAR, fines); 
    }

    // This is optional if your parent class already has getVehicleType
    @Override
    public VehicleType getVehicleType() {
        return VehicleType.CAR;
    }
}
