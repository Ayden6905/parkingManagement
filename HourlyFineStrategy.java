/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author ayden
 */
public class HourlyFineStrategy implements FineStrategy {

    @Override
    public double calculateFine(long totalHours) {
        if (totalHours > 24) {
            long overstayHours = totalHours - 24;
            return overstayHours * 20.0;
        }
        return 0.0;
    }
}