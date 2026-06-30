package com.reservation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model class representing a Bus.
 * Demonstrates Encapsulation with private attributes and public getters/setters.
 */
public class Bus {
    private int id;
    private String busNumber;
    private String source;
    private String destination;
    private String departureTime;
    private int totalSeats;
    private int availableSeats;
    private double fare;
    private String busType;
    private String reservedSeats; // Comma-separated string of seat numbers e.g. "1,2,5"

    // Default Constructor
    public Bus() {
    }

    // Parametrized Constructor
    public Bus(int id, String busNumber, String source, String destination, String departureTime, 
               int totalSeats, int availableSeats, double fare, String busType, String reservedSeats) {
        this.id = id;
        this.busNumber = busNumber;
        this.source = source;
        this.destination = destination;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.fare = fare;
        this.busType = busType;
        this.reservedSeats = reservedSeats;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public String getBusType() {
        return busType;
    }

    public void setBusType(String busType) {
        this.busType = busType;
    }

    public String getReservedSeats() {
        return reservedSeats == null ? "" : reservedSeats;
    }

    public void setReservedSeats(String reservedSeats) {
        this.reservedSeats = reservedSeats;
    }

    // Helper methods for seat status
    
    /**
     * Parse the comma-separated reserved seats string into a List of Integers.
     */
    public List<Integer> getReservedSeatsList() {
        List<Integer> list = new ArrayList<>();
        String seatsStr = getReservedSeats();
        if (seatsStr.trim().isEmpty()) {
            return list;
        }
        String[] parts = seatsStr.split(",");
        for (String part : parts) {
            try {
                list.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException e) {
                // Ignore invalid numbers
            }
        }
        Collections.sort(list);
        return list;
    }

    /**
     * Checks if a seat number is already reserved.
     */
    public boolean isSeatReserved(int seatNum) {
        return getReservedSeatsList().contains(seatNum);
    }
}
