/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author ayden
 */
public class ProgressiveFineStrategy implements FineStrategy {

    @Override
    public double calculateFine(long totalHours) {
        if (totalHours <= 24) {
            return 0.0;
        }

        long overstayHours = totalHours - 24;
        double fine = 50.0; // Base fine for the first 24h of overstay

        // If more than 24 hours (Total > 48)
        if (overstayHours > 24) {
            fine += 100.0;
        }
        
        // If more than 48 hours (Total > 72)
        if (overstayHours > 48) {
            fine += 150.0;
        }
        
        // If more than 72 hours (Total > 96)
        if (overstayHours > 72) {
            fine += 200.0;
        }

        return fine;
    }
}