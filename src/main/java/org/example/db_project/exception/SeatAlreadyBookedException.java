package org.example.db_project.exception;

import java.util.List;

public class SeatAlreadyBookedException extends RuntimeException {
    private final List<Long> bookedSeatIds;

    public SeatAlreadyBookedException(List<Long> bookedSeatIds) {
        super("Seats already booked: " + bookedSeatIds);
        this.bookedSeatIds = bookedSeatIds;
    }

    public SeatAlreadyBookedException(Long seatId) {
        super("Seat already booked: " + seatId);
        this.bookedSeatIds = List.of(seatId);
    }

    public List<Long> getBookedSeatIds() {
        return bookedSeatIds;
    }
}
