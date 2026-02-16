/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author User
 */
public class VehicleFactory {
    public Vehicle createVehicle(String type, String plate, double debt) {
        if (type == null) return new Car(plate, debt);
        
        switch (type.toUpperCase()) {
            case "MOTORCYCLE": return new Motorcycle(plate, debt);
            case "SUV":        return new SUV(plate, debt);
            case "HANDICAPPED": return new HandicappedVehicle(plate, debt);
            default:           return new Car(plate, debt);
        }
    }
    
    // Overload for convenience
    public Vehicle createVehicle(String type, String plate) {
        return createVehicle(type, plate, 0.0);
    }
}