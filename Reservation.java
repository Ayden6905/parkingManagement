/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parkingmanagement;
/**
 *
 * @author HP
 */

import java.time.LocalDateTime;

public class Reservation {
    private String reservationId;
    private String licensePlate;
    private ReservedSpot spotId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    
    public Reservation(String reservationId, String licensePlate, ReservedSpot spotId,
            LocalDateTime startTime, LocalDateTime endTime, ReservationStatus status)
    {
        this.reservationId = reservationId;
        this.licensePlate = licensePlate;
        this.spotId = spotId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
    
    public String getLicensePlate()
    {
        return licensePlate;
    }
    
    public boolean isValid(LocalDateTime currentTime)
    {
        if (status != ReservationStatus.ACTIVE)
            return false;
        if (currentTime == null)
            return false;
        return (!currentTime.isBefore(startTime)) && (!currentTime.isAfter(endTime));
    }
    
    public boolean matchesSpot(ParkingSpot spot)
    {
        if (spot == null || spotId == null)
            return false;
        return spot.getSpotId().equals(spotId.getSpotId());
    }
    
    public void markUsed()
    {
        this.status = ReservationStatus.USED;
    }
}
