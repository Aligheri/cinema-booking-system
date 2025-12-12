package org.example.db_project.service;

import lombok.RequiredArgsConstructor;
import org.example.db_project.domain.entity.Seat;
import org.example.db_project.domain.repository.BookingSeatRepository;
import org.example.db_project.domain.repository.SeatRepository;
import org.example.db_project.dto.response.SeatResponse;
import org.example.db_project.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;

    public List<SeatResponse> getSeatsByHall(Long hallId) {
        return seatRepository.findByHallIdOrdered(hallId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getAvailableSeatsForSession(Long hallId, Long sessionId) {
        List<Long> bookedSeatIds = bookingSeatRepository.findBookedSeatIdsForSession(sessionId);
        Set<Long> bookedSeatIdSet = Set.copyOf(bookedSeatIds);
        return seatRepository.findByHallIdOrdered(hallId)
                .stream()
                .map(seat -> toResponse(seat, !bookedSeatIdSet.contains(seat.getId())))
                .toList();
    }

    public int countAvailableSeats(Long hallId, Long sessionId) {
        return seatRepository.countAvailableSeatsForSession(hallId, sessionId);
    }

    @Transactional
    public List<Seat> findByIdsWithLock(List<Long> seatIds) {
        List<Seat> seats = seatRepository.findByIdsWithLock(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new ResourceNotFoundException("Some seats not found");
        }
        return seats;
    }

    public SeatResponse toResponse(Seat seat) {
        return toResponse(seat, true);
    }

    public SeatResponse toResponse(Seat seat, boolean available) {
        return SeatResponse.builder()
                .id(seat.getId())
                .rowNumber(seat.getRowNumber())
                .seatNumber(seat.getSeatNumber())
                .seatType(seat.getSeatType())
                .priceMultiplier(seat.getPriceMultiplier())
                .seatLabel(seat.getSeatLabel())
                .available(available)
                .build();
    }
}
