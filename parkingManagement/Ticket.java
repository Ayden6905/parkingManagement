/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parkingManagement;

/**
 *
 * @author HP
 */

import java.time.LocalDateTime;

public class Ticket {
    private String ticketId;
    private LocalDateTime entryTime;
    private Vehicle licensePlate;
    private ParkingSpot spotId;
    
    public Ticket(Vehicle v, ParkingSpot s)
    {
        this.licensePlate = v;
        this.spotId = s;
        this.entryTime = LocalDateTime.now();
        this.ticketId = generateTicketId();
    }
    
    public String getTicketId()
    {
        return ticketId;
    }
    
    public LocalDateTime getEntryTime()
    {
        return entryTime;
    }
    
    public String generateTicketId()
    {
        return "T-" + licensePlate.getLicensePlate() + "-" + System.currentTimeMillis();
    }
}
