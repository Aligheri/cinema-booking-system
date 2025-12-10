package org.example.db_project.exception;

import org.example.db_project.domain.enums.BookingStatus;

public class InvalidBookingStateException extends RuntimeException {
    public InvalidBookingStateException(String message) {
        super(message);
    }

    public InvalidBookingStateException(BookingStatus currentStatus, BookingStatus targetStatus) {
        super("Cannot transition booking from " + currentStatus + " to " + targetStatus);
    }
}
