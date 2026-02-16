/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parkingmanagement;
/**
 *
 * @author ayden
 */
public interface FineStrategy {
    // Calculates fine based on TOTAL hours parked
    double calculateFine(long totalHours);
}
