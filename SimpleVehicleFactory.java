/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parking_management;

/**
 *
 * @author ayden
 */

// This class creates obj of vehicles
public class SimpleVehicleFactory {
    
    // Static method to create the correct subclass based on a string input
    public static Vehicle createVehicle(String licensePlate, String typeStr) {
        if (typeStr == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }

        switch (typeStr.toUpperCase()) {
            case "CAR":
                return new Car(licensePlate);
            case "MOTORCYCLE":
                return new Motorcycle(licensePlate);
            case "SUV":
                return new SUV(licensePlate);
            case "HANDICAPPED":
                return new HandicappedVehicle(licensePlate);
            default:
                throw new IllegalArgumentException("Unknown vehicle type: " + typeStr);
        }
    }
}