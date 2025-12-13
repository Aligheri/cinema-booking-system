package org.example.db_project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.db_project.domain.entity.*;
import org.example.db_project.domain.enums.BookingStatus;
import org.example.db_project.domain.repository.*;
import org.example.db_project.dto.request.CreateBookingRequest;
import org.example.db_project.dto.request.UpdateBookingStatusRequest;
import org.example.db_project.dto.response.*;
import org.example.db_project.exception.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final SeatService seatService;
    private final HallService hallService;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        log.info("Creating booking for user {} on session {} with {} seats",
                request.getUserId(), request.getSessionId(), request.getSeatIds().size());
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));
        Session session = sessionRepository.findByIdWithDetails(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session", request.getSessionId()));
        if (!session.isAvailableForBooking()) {
            throw new SessionNotAvailableException(session.getId());
        }
        List<Seat> seats = seatRepository.findByIdsWithLock(request.getSeatIds());
        if (seats.size() != request.getSeatIds().size()) {
            throw new ResourceNotFoundException("Some seats not found");
        }
        Long hallId = session.getHall().getId();
        for (Seat seat : seats) {
            if (!seat.getHall().getId().equals(hallId)) {
                throw new IllegalStateException(
                        "Seat " + seat.getId() + " does not belong to hall " + hallId);
            }
        }
        List<Long> alreadyBookedSeatIds = new ArrayList<>();
        for (Seat seat : seats) {
            if (bookingSeatRepository.isSeatBookedForSession(seat.getId(), session.getId())) {
                alreadyBookedSeatIds.add(seat.getId());
            }
        }
        if (!alreadyBookedSeatIds.isEmpty()) {
            throw new SeatAlreadyBookedException(alreadyBookedSeatIds);
        }
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<BookingSeat> bookingSeats = new ArrayList<>();
        for (Seat seat : seats) {
            BigDecimal seatPrice = session.getBasePrice()
                    .multiply(seat.getPriceMultiplier());
            totalPrice = totalPrice.add(seatPrice);
            BookingSeat bookingSeat = BookingSeat.builder()
                    .seat(seat)
                    .price(seatPrice)
                    .build();
            bookingSeats.add(bookingSeat);
        }
        Booking booking = Booking.builder()
                .user(user)
                .session(session)
                .totalPrice(totalPrice)
                .status(BookingStatus.PENDING)
                .build();
        for (BookingSeat bookingSeat : bookingSeats) {
            booking.addBookingSeat(bookingSeat);
        }
        booking = bookingRepository.save(booking);
        log.info("Booking created with id: {}, total price: {}", booking.getId(), totalPrice);
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse updateBookingStatus(Long bookingId, UpdateBookingStatusRequest request) {
        log.info("Updating booking {} status to {}", bookingId, request.getNewStatus());
        Booking booking = bookingRepository.findByIdWithOptimisticLock(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        BookingStatus currentStatus = booking.getStatus();
        BookingStatus newStatus = request.getNewStatus();
        if (!booking.canTransitionTo(newStatus)) {
            throw new InvalidBookingStateException(currentStatus, newStatus);
        }
        booking.transitionTo(newStatus);
        booking = bookingRepository.save(booking);
        log.info("Booking {} status updated from {} to {}", bookingId, currentStatus, newStatus);
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        return updateBookingStatus(bookingId,
                UpdateBookingStatusRequest.builder()
                        .newStatus(BookingStatus.CONFIRMED)
                        .build());
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        log.info("Cancelling booking: {}", bookingId);
        Booking booking = bookingRepository.findByIdWithOptimisticLock(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        if (!booking.canTransitionTo(BookingStatus.CANCELLED)) {
            throw new InvalidBookingStateException(booking.getStatus(), BookingStatus.CANCELLED);
        }
        booking.transitionTo(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking cancelled: {}", bookingId);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getUserBookings(Long userId, int page, int size) {
        return bookingRepository.findUserBookingsWithDetails(userId, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Transactional
    public int expirePendingBookings(int minutesOld) {
        OffsetDateTime expirationTime = OffsetDateTime.now().minusMinutes(minutesOld);
        List<Booking> expiredBookings = bookingRepository.findExpiredPendingBookings(expirationTime);
        if (expiredBookings.isEmpty()) {
            return 0;
        }
        List<Long> expiredIds = expiredBookings.stream()
                .map(Booking::getId)
                .toList();
        int count = bookingRepository.expireBookings(expiredIds);
        log.info("Expired {} pending bookings older than {} minutes", count, minutesOld);
        return count;
    }

    private BookingResponse toResponse(Booking booking) {
        Session session = booking.getSession();
        UserResponse userResponse = UserResponse.builder()
                .id(booking.getUser().getId())
                .email(booking.getUser().getEmail())
                .firstName(booking.getUser().getFirstName())
                .lastName(booking.getUser().getLastName())
                .fullName(booking.getUser().getFullName())
                .build();
        MovieResponse movieResponse = MovieResponse.builder()
                .id(session.getMovie().getId())
                .title(session.getMovie().getTitle())
                .durationMinutes(session.getMovie().getDurationMinutes())
                .build();
        SessionResponse sessionResponse = SessionResponse.builder()
                .id(session.getId())
                .movie(movieResponse)
                .hall(hallService.toResponse(session.getHall()))
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .basePrice(session.getBasePrice())
                .status(session.getStatus())
                .build();
        List<BookingSeatResponse> seatResponses = booking.getBookingSeats().stream()
                .map(bs -> BookingSeatResponse.builder()
                        .id(bs.getId())
                        .seat(seatService.toResponse(bs.getSeat()))
                        .price(bs.getPrice())
                        .build())
                .toList();
        return BookingResponse.builder()
                .id(booking.getId())
                .user(userResponse)
                .session(sessionResponse)
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .seats(seatResponses)
                .createdAt(booking.getCreatedAt())
                .ticketCount(booking.getTicketCount())
                .build();
    }
}
