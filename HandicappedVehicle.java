/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingmanagement;

/**
 *
 * @author ayden
 */
public class HandicappedVehicle extends Vehicle {
    
    // Constructor matching the parent Vehicle class
    public HandicappedVehicle(String licensePlate) {
        super(licensePlate);
    }

    // This is the most important part: Identifying itself
    @Override
    public VehicleType getVehicleType() {
        return VehicleType.HANDICAPPED; 
    }
}
