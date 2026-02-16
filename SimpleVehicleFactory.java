/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author ayden
 */

// This class creates obj of vehicles
public class SimpleVehicleFactory {
    
    // Updated method signature to include fines
    public static Vehicle createVehicle(String licensePlate, String typeStr, double fines) {
        if (typeStr == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }

        switch (typeStr.toUpperCase()) {
            case "CAR":
                return new Car(licensePlate, fines); // Pass fines here
            case "MOTORCYCLE":
                return new Motorcycle(licensePlate, fines);
            case "SUV":
                return new SUV(licensePlate, fines);
            case "HANDICAPPED":
                return new HandicappedVehicle(licensePlate, fines);
            default:
                throw new IllegalArgumentException("Unknown vehicle type: " + typeStr);
        }
    }
}