/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

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
    private ParkingSpot createSpotByRow(String spotId, int floorNumber, int row)
    {
        switch (row)
        {
            case 1:
                return new ReservedSpot(spotId, floorNumber);
            case 2:
                return new CompactSpot(spotId, floorNumber);
            case 3:
                return new HandicappedSpot(spotId, floorNumber);
            case 4:
                return new RegularSpot(spotId, floorNumber);
            default:
                throw new IllegalArgumentException("Invalid row: " + row);
        }
    }
    
    // to let other to add reservations
    public void addReservation(Reservation r)
    {
        reservations.add(r);
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
    
    public Ticket parkVehicle(Vehicle v, ParkingSpot s)
    {
        if (v == null || s == null) return null;
        
        // reserved spot must have reservation
        if (s instanceof ReservedSpot)
        {
            Reservation r = findValidReservationFor(v, s, LocalDateTime.now());
            if (r == null) return null;
            r.markUsed();
        }
        
        if (!s.isAvailable()) return null;
        if (!s.canParkVehicle(v)) return null;
        
        s.parkVehicle(v);
        
        return new Ticket(
                "T-" + v.getLicensePlate() + "-" + System.currentTimeMillis(),
                v, s, LocalDateTime.now()
        );
    }
    
    public Receipt exitVehicle(String licensePlate) 
{
        // find the ticket
        Ticket t = Ticket.findActiveByPlate(licensePlate);

        if (t == null) return null; 

        // stub for now
        LocalDateTime exitTime = LocalDateTime.now();
        double parkingFee = 0.0;
        double fineAmount = 0.0;
        double totalPaid = 0.0;
        String paymentMethod = "N/A";
        double remainingBalance = 0.0;

        t.closeTicket(exitTime, parkingFee, fineAmount, totalPaid, paymentMethod);

        // create receipt
        return new Receipt(t, parkingFee, fineAmount, totalPaid, t.getPaymentMethod());
    }
    
    public ParkingSpot findSpotById(String spotId)
    {        
        String sql = "SELECT spotId, floorNumber, spotType, hourlyRate FROM parkingSpot WHERE spotId = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, spotId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String type = rs.getString("spotType");
                int floor = rs.getInt("floorNumber");
                double rate = rs.getDouble("hourlyRate");

                switch(type.toUpperCase()) {
                    case "REGULAR": return new RegularSpot(spotId, floor);
                    case "HANDICAPPED": return new HandicappedSpot(spotId, floor);
                    case "RESERVED": return new ReservedSpot(spotId, floor);
                    case "COMPACT": return new CompactSpot(spotId, floor);
                    default: throw new IllegalArgumentException("Unknown spot type: " + type);
                }
            }

        } catch (SQLException e) {
            System.out.println("Find spot error: " + e.getMessage());
        }
        return null;
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