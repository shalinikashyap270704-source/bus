package com.reservation.exception;

/**
 * Container class for custom exceptions used in the application.
 */
public class CustomExceptions {

    /**
     * Exception thrown when user input is invalid, empty, or formatted incorrectly.
     */
    public static class InvalidInputException extends Exception {
        public InvalidInputException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown when seat booking operations fail (e.g., seat already taken, invalid seat number).
     */
    public static class SeatBookingException extends Exception {
        public SeatBookingException(String message) {
            super(message);
        }
    }
}
