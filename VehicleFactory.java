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
        return createVehicle(type, plate, 0.0);
    }

    public Vehicle createVehicle(String type, String plate, double debt) {
        Vehicle v;
        String vehicleType = (type == null) ? "car" : type.toLowerCase();

        switch (vehicleType) {
            case "motorcycle":
                v = new Motorcycle(plate, debt);
                break;
            case "suv":
                v = new SUV(plate, debt);
                break;
            default:
                v = new Car(plate, debt);
                break;
        }
        return v; // MUST have this line!
    }
}