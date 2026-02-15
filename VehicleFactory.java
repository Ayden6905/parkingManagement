/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parkingmanagement;
/**
 *
 * @author User
 */
public class VehicleFactory {
    
    public Vehicle createVehicle(String type, String plate) {

        switch (type) {
            case "Car":
                return new Car(plate);
            case "Motorcycle":
                return new Motorcycle(plate);
            case "SUV":
                return new SUV(plate);
            case "Handicapped":
                return new HandicappedVehicle(plate);
            default:
                throw new IllegalArgumentException("Unknown vehicle type: " + type);
        }
    }
}
