/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;
/**
 *
 * @author NurqistinaAtashah
 */

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


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
            return new CompactSpot(spotId, floorNumber);
        case 2:
            return new ReservedSpot(spotId, floorNumber); 
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
    
    
    
     
public Receipt exitVehicle(String licensePlate) {
    //Find active ticket
    Ticket t = Ticket.findActiveByPlate(licensePlate);

    if (t == null) return null; 

    // Identify the spot and release iti n memory
        ParkingSpot spot = t.getSpot();
    if (spot != null) {
        spot.setStatus(SpotStatus.AVAILABLE);
        ParkingRepository repo = new ParkingRepository();
        repo.releaseSpot(spot.getSpotId());
    }

    // Existing logic
    LocalDateTime exitTime = LocalDateTime.now();
    double parkingFee = 0.0; 
    double fineAmount = 0.0;
    double totalPaid = 0.0;
    String paymentMethod = "N/A";

    t.closeTicket(exitTime, parkingFee, fineAmount, totalPaid, paymentMethod);

    // Create receipt
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
    for (Floor floor : floors) { 
        for (ParkingSpot spot : floor.getAllSpots()) {
            allSpotsMap.put(spot.getSpotId(), spot);
        }
    }
    return allSpotsMap;
}
    
    public double calculateOccupancy() {
    int total = 0;
    int occupied = 0;
    
    for (Floor f : floors) {
        total += f.getAllSpots().size();
        // Uses existing Floor method to count occupied spots
        occupied += f.getOccupiedSpots().size(); 
    }
    return total == 0 ? 0.0 : ((double) occupied / total);
}
    
    public void setFineStrategy(FineStrategy strategy)
    {
    }
    
public Ticket parkVehicle(Vehicle v, ParkingSpot s, String scheme) { 
    if (v == null || s == null) return null;
    
    // Check availability
    if (!s.isAvailable()) return null;
    if (!s.canParkVehicle(v)) return null;
    
    double existingDebt = 0.0;
    FineManager fm = new FineManager(); 
    existingDebt = fm.getOutstandingFineByPlate(v.getLicensePlate());
    s.parkVehicle(v); //Mark occupied
    
    // Create a new ticket with a unique timestamp-based ID
    return new Ticket(
            "T-" + v.getLicensePlate() + "-" + System.currentTimeMillis(),
            v, 
            s, 
            LocalDateTime.now(),
            scheme,       
            existingDebt  
    );
}
    public List<ParkingSpot> getAllSpots() {
        ParkingRepository repo = new ParkingRepository();
        return repo.getAllParkingSpots();
    }
    
    
    public List<ParkingSpot> getAvailableSpots(Vehicle v, String plate) {
        List<ParkingSpot> result = new ArrayList<>();

        //Check plate currently has an ACTIVE reservation
        ParkingRepository repo = new ParkingRepository();
        List<String> reservedIds = repo.getReservedSelectableSpotIds(plate);
        boolean hasReservationNow = reservedIds != null && !reservedIds.isEmpty();

        for (Floor floor : floors) {
            for (ParkingSpot spot : floor.getAllSpots()) {
                // Hide OCCUPIED spots
                if (!spot.isAvailable()) {
                    continue;
                }
                // ignore vehicle suitability 
                if (!spot.canParkVehicle(v)) {
                    continue;
                }
                result.add(spot);
            }
        }

        return result;
    }

    
    public List<String> getReservedSpotIdsForPlate(String plate) {
        List<String> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        if (plate == null) {
            return result;
        }
        plate = plate.trim();
        if (plate.isEmpty()) {
            return result;
        }

        for (Reservation r : reservations) {
            if (r.getLicensePlate().equalsIgnoreCase(plate)
                    && r.getStatus() == ReservationStatus.ACTIVE
                    && !now.isBefore(r.getStartTime())
                    && !now.isAfter(r.getEndTime())) {

                String spotId = r.getSpotId().getSpotId();
                ParkingSpot spot = findSpotById(spotId);
                if (spot != null && spot.isAvailable()) {
                    result.add(spotId);
                }
            }
        }
        return result;
    }
    

public String getFormattedSpotName(ParkingSpot spot) {
    String type = "Regular"; 
    if (spot instanceof CompactSpot) type = "Compact";
    else if (spot instanceof ReservedSpot) type = "Reserved";
    else if (spot instanceof HandicappedSpot) type = "Handicapped";
    
    return spot.getSpotId() + " (" + type + ")";
}


//to see spots based on vehicleType
public List<ParkingSpot> getAvailableAndReservedSpots(Vehicle v, String plate) {
    List<ParkingSpot> compatibleSpots = new ArrayList<>();
    
    for (Floor floor : floors) {
        for (ParkingSpot s : floor.getAllSpots()) {
            
            // 1. If someone is already parked there, nobody else can.
            if (!s.isAvailable()) continue;

            // 2. Logic: Who can see this spot?
            boolean showSpot = false;

            // Rule A: Reserved spots open to everyone 
            if (s instanceof ReservedSpot) {
                showSpot = true; 
            }
            // Rule B: Handicapped people can see everything
            else if (v.isHandicappedCardHolder()) {
                showSpot = true;
            }
            // Rule C: Standard Type Compatibility
            else {
                if (v instanceof Motorcycle && s instanceof CompactSpot) showSpot = true;
                else if (v instanceof SUV && s instanceof RegularSpot) showSpot = true;
                else if (v instanceof Car && (s instanceof RegularSpot || s instanceof CompactSpot)) showSpot = true;
            }

            if (showSpot) {
                compatibleSpots.add(s);
            }
        }
    }
    return compatibleSpots;
}

}