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
    
    // Add 'fine' as a parameter here
    public Vehicle createVehicle(String type, String plate, double fine) {
        switch (type) {
            case "Car":
                return new Car(plate, fine); 
            case "Motorcycle":
                return new Motorcycle(plate, fine);
            case "SUV":
                return new SUV(plate, fine);
            case "Handicapped":
                return new HandicappedVehicle(plate, fine);
            default:
                throw new IllegalArgumentException("Unknown vehicle type: " + type);
        }
    }
}
