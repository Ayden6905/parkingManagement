/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parkingmanagement;

/**
 *
 * @author NurqistinaAtashah
 */

public class FineManager {
    // Member 4: This is a placeholder for Member 2's logic
    public double calculateFine(String violationType) {
        if (violationType.equals("LOST_TICKET")) {
            return 50.00;
        }
        return 0.00;
    }
}
