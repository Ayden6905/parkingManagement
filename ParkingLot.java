/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parkingmanagement;
/**
 *
 * @author NurqistinaAtashah
 */

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ParkingLot {
    private String parkingLotId;
    private List<Floor> floors;
    private double totalRevenue;
    
    private List<Reservation> reservations;
    private static ParkingLot instance;
    
    private ParkingLot()
    {
        this.parkingLotId = "PL-1";
        this.floors = new ArrayList<>();
        this.reservations = new ArrayList<>();
        addDefaultFloors(5);
    }
    
    public static ParkingLot getInstance()
    {
        if (instance == null)
        {
            instance = new ParkingLot();
        }
        return instance;
    }
    
    public ParkingLot(int numberOfFloors)
    {
        this.parkingLotId = "PL-1";
        this.floors = new ArrayList<>();
        this.reservations = new ArrayList<>();
        addDefaultFloors(numberOfFloors);
    }
    
    // ADDED: The missing method for your ReservationPanel
    public void addReservation(Reservation r) {
        if (r != null) {
            this.reservations.add(r);
        }
    }
    
    public List<Reservation> getReservations() {
        return reservations;
    }
    
    public void addFloor(Floor floor)
    {
        floors.add(floor);
    }
    
    // build parking layout
    private void addDefaultFloors(int numberOfFloors)
    {
        for (int f = 1; f <= numberOfFloors; f++)
        {
            Floor floor = new Floor(f);
            
            for (int row = 1; row <= 4; row++)
            {
                for (int s = 1; s <= 10; s++)
                {
                    String spotId = "F" + f + "-R" + row + "-S" + s;
                    floor.addSpot(createSpotByRow(spotId, f, row));
                }
            }
            addFloor(floor);
        }
    }
    
    //assigning spot type
   private ParkingSpot createSpotByRow(String spotId, int floorNumber, int row) {
    switch (row) {
        case 1:
            return new CompactSpot(spotId, floorNumber); // Row 1 is Compact in your DB
        case 2:
            return new ReservedSpot(spotId, floorNumber); // Row 2 is Reserved in your DB
        case 3:
            return new HandicappedSpot(spotId, floorNumber);
        case 4:
            return new RegularSpot(spotId, floorNumber);
        case 5:
            return new RegularSpot(spotId, floorNumber);
        default:
            throw new IllegalArgumentException("Invalid row: " + row);
    }
}
    
    
    
    //search for spot
    public List<ParkingSpot> getAvailableSpots(Vehicle v)
    {
        List<ParkingSpot> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (Floor floor : floors)
        {
            for (ParkingSpot spot : floor.getAllSpots())
            {
                if (!spot.isAvailable()) continue;
                if (!spot.canParkVehicle(v)) continue;
                
                //check for reservation (only for ReservedSpot)
                if (spot instanceof ReservedSpot)
                {
                    Reservation r = findValidReservationFor(v, spot, now);
                    if (r == null) continue; // no reservation, cannot use reserved spot
                }
                
                result.add(spot);
            }
        }
        return result;
    }
    
    //find the reservation
    private Reservation findValidReservationFor(Vehicle v, ParkingSpot spot, LocalDateTime now)
    {
        for (Reservation r : reservations)
        {
            if (r.getLicensePlate().equals(v.getLicensePlate())
                && r.isValid(now) && r.matchesSpot(spot))
                        {
                            return r;
                        }
        }
        return null;
    }
    
     
public Receipt exitVehicle(String licensePlate) {
    // 1. Find the active ticket
    Ticket t = Ticket.findActiveByPlate(licensePlate);

    if (t == null) return null; 

    // 2. Identify the spot and release it in memory
    // This makes the spot available for the next car immediately in the UI
    ParkingSpot spot = t.getSpot();
    if (spot != null) {
        spot.setStatus(SpotStatus.AVAILABLE);
        
        // 3. Release the spot in the Database
        // This ensures the AdminPanel 'Refresh' shows the correct count
        ParkingRepository repo = new ParkingRepository();
        repo.releaseSpot(spot.getSpotId());
    }

    // Existing logic
    LocalDateTime exitTime = LocalDateTime.now();
    double parkingFee = 0.0; // You can add your calculation logic here later
    double fineAmount = 0.0;
    double totalPaid = 0.0;
    String paymentMethod = "N/A";

    t.closeTicket(exitTime, parkingFee, fineAmount, totalPaid, paymentMethod);

    // 4. Create receipt
    return new Receipt(t, parkingFee, fineAmount, totalPaid, t.getPaymentMethod());
}

    
    public ParkingSpot findSpotById(String spotId) {
    for (Floor floor : floors) {
        for (ParkingSpot spot : floor.getAllSpots()) {
            if (spot.getSpotId().equalsIgnoreCase(spotId)) {
                return spot;
            }
        }
    }
    return null;
}
    
    public java.util.Map<String, ParkingSpot> getSpots() {
    java.util.Map<String, ParkingSpot> allSpotsMap = new java.util.HashMap<>();
    // This iterates through the floors you built in the constructor
    for (Floor floor : floors) { 
        for (ParkingSpot spot : floor.getAllSpots()) {
            allSpotsMap.put(spot.getSpotId(), spot);
        }
    }
    return allSpotsMap;
}
    
    public double calculateOccupancy()
    {
        int total = 0;
        int occupied = 0;
        
        for (Floor f : floors)
        {
            total += f.getAllSpots().size();
            occupied += f.getOccupiedSpots().size();
        }
        return total == 0 ? 0.0 : (occupied * 1.0/total);
    }
    
    public void setFineStrategy(FineStrategy strategy)
    {
    }
    
    // Update the method signature to accept 'scheme'
// Update the method to include the scheme
public Ticket parkVehicle(Vehicle v, ParkingSpot s, String scheme) { 
    if (v == null || s == null) return null;
    
    // 1. Check availability
    if (!s.isAvailable()) return null;
    if (!s.canParkVehicle(v)) return null;
    
    // 2. NEW: Fetch the vehicle's outstanding debt from the database
    // This ensures the ticket knows if the user owes money from a previous visit
    double existingDebt = 0.0;
    FineManager fm = new FineManager(); // Or use a shared instance if available
    existingDebt = fm.getOutstandingFineByPlate(v.getLicensePlate());
    
    // 3. Mark the spot as occupied
    s.parkVehicle(v);
    
    // 4. FIX: Provide all 6 arguments to the Ticket constructor
    return new Ticket(
            "T-" + v.getLicensePlate() + "-" + System.currentTimeMillis(),
            v, 
            s, 
            LocalDateTime.now(),
            scheme,       // Argument 5
            existingDebt  // Argument 6: The Carried-Over Fine
    );
}
    public List<ParkingSpot> getAllSpots() {
        ParkingRepository repo = new ParkingRepository();
        return repo.getAllParkingSpots();
    }
    
    
    private boolean hasAnyActiveReservationForPlate(String plate, LocalDateTime now) {

        if (plate == null) {
            return false;
        }

        plate = plate.trim();
        if (plate.isEmpty()) {
            return false;
        }

        for (Reservation r : reservations) {

            if (r.getLicensePlate().equalsIgnoreCase(plate)
                    && r.getStatus() == ReservationStatus.ACTIVE
                    && !now.isBefore(r.getStartTime())
                    && !now.isAfter(r.getEndTime())) {

                return true;
            }
        }
        return false;
    }
    
    public List<ParkingSpot> getAvailableSpots(Vehicle v, String plate) {

        List<ParkingSpot> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        boolean hasReservation = hasAnyActiveReservationForPlate(plate, now);

        for (Floor floor : floors) {
            for (ParkingSpot spot : floor.getAllSpots()) {

                if (!spot.isAvailable()) {
                    continue;
                }
                if (!spot.canParkVehicle(v)) {
                    continue;
                }
                
                if (spot instanceof ReservedSpot && !hasReservation) {
                    continue;
                }
                result.add(spot);
            }
        }
        return result;
    }
    

}