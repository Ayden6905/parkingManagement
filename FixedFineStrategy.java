/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author ayden
 */
public class FixedFineStrategy implements FineStrategy {
    
    @Override
    public double calculateFine(long totalHours) {
        if (totalHours > 24) {
            return 50.0;
        }
        return 0.0;
    }
}