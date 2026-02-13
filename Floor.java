/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author HP
 */

import java.util.ArrayList;
import java.util.List;

public class Floor {
    private int floorNumber;
    private List<ParkingSpot> spots;
    
    public Floor(int floorNumber)
    {
        this.floorNumber = floorNumber;
        this.spots = new ArrayList<>();
    }
    
    public int getFloorNumber()
    {
        return floorNumber;
    }
    
    public void addSpot(ParkingSpot spot)
    {
        spots.add(spot);
    }
    
    public List<ParkingSpot> getAvailableSpots()
    {
        List<ParkingSpot> list = new ArrayList<>();
        
        for (ParkingSpot s : spots)
        {
            if (s.isAvailable())
                list.add(s);
        }
        return list;
    }
    
    public List<ParkingSpot> getOccupiedSpots()
    {
        List<ParkingSpot> list = new ArrayList<>();
        
        for (ParkingSpot s : spots)
        {
            if (!s.isAvailable())
                list.add(s);
        }
        return list;
    }
    
    // help to search parking lot (get all parking spots)
    public List<ParkingSpot> getAllSpots()
    {
        return spots;
    }
}
