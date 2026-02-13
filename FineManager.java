/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author NurqistinaAtashah
 */
/**
 * MEMBER 2's MODULE: Fine Management (Strategy Pattern Context)
 */
public class FineManager {
    // 1. Hold a reference to the Strategy Interface
    private FineStrategy currentStrategy;

    public FineManager() {
        // Default to 'Fixed' scheme when the system starts
        this.currentStrategy = new FixedFineStrategy();
    }

    // 2. Allow the Admin to switch schemes at runtime
    public void setStrategy(String schemeName) {
        switch (schemeName) {
            case "Fixed":
                this.currentStrategy = new FixedFineStrategy();
                break;
            case "Progressive":
                this.currentStrategy = new ProgressiveFineStrategy();
                break;
            case "Hourly":
                this.currentStrategy = new HourlyFineStrategy();
                break;
            default:
                System.out.println("Unknown scheme: " + schemeName + ". Keeping current strategy.");
        }
    }

    // 3. The method that Facade will call
    public double calculateFine(long totalHours) {
        // Delegate the calculation to the chosen strategy
        return currentStrategy.calculateFine(totalHours);
    }
}