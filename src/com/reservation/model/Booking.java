package com.reservation.model;

/**
 * Model class representing a ticket booking reservation.
 * Demonstrates encapsulation with private attributes and public getters/setters.
 */
public class Booking {
    private int bookingId;
    private String customerName;
    private String customerPhone;
    private int busId;
    private String seatNumbers; // Comma-separated list of seat numbers e.g. "10,11"
    private String bookingDate;
    private String status; // ACTIVE or CANCELLED
    private double totalAmount;

    // Default Constructor
    public Booking() {
    }

    // Parametrized Constructor
    public Booking(int bookingId, String customerName, String customerPhone, int busId, 
                   String seatNumbers, String bookingDate, String status, double totalAmount) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.busId = busId;
        this.seatNumbers = seatNumbers;
        this.bookingDate = bookingDate;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    // Getters and Setters
    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public int getBusId() {
        return busId;
    }

    public void setBusId(int busId) {
        this.busId = busId;
    }

    public String getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(String seatNumbers) {
        this.seatNumbers = seatNumbers;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
