/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parking_management;

/**
 *
 * @author NurqistinaAtashah
 */
public class Receipt {
    private String plate;
    private int hours;
    private double total;

    public Receipt(String plate, int hours, double total) {
        this.plate = plate;
        this.hours = hours;
        this.total = total;
    }

    // This makes it easy to show in a JOptionPane
    public String toString() {
        return "--- PARKING RECEIPT ---\n" +
               "Plate: " + plate + "\n" +
               "Hours: " + hours + "\n" +
               "Total Paid: RM " + String.format("%.2f", total);
    }
}