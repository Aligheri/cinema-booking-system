package org.example.db_project.service;

import org.example.db_project.BaseIntegrationTest;
import org.example.db_project.domain.entity.*;
import org.example.db_project.domain.enums.*;
import org.example.db_project.domain.repository.*;
import org.example.db_project.dto.request.CreateBookingRequest;
import org.example.db_project.dto.request.UpdateBookingStatusRequest;
import org.example.db_project.dto.response.BookingResponse;
import org.example.db_project.exception.InvalidBookingStateException;
import org.example.db_project.exception.SeatAlreadyBookedException;
import org.example.db_project.exception.SessionNotAvailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

class BookingServiceIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private HallRepository hallRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private BookingRepository bookingRepository;
    private User testUser;
    private Session testSession;
    private List<Seat> testSeats;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        sessionRepository.deleteAll();
        testUser = userRepository.findByEmail("admin@cinema.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("test@test.com")
                        .passwordHash("hash")
                        .firstName("Test")
                        .lastName("User")
                        .role(UserRole.USER)
                        .build()));
        Movie movie = movieRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No movies found"));
        Hall hall = hallRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No halls found"));
        testSession = sessionRepository.save(Session.builder()
                .movie(movie)
                .hall(hall)
                .startTime(OffsetDateTime.now().plusDays(1))
                .endTime(OffsetDateTime.now().plusDays(1).plusHours(2))
                .basePrice(new BigDecimal("10.00"))
                .status(SessionStatus.SCHEDULED)
                .build());
        testSeats = seatRepository.findByHallIdOrdered(hall.getId()).subList(0, 3);
    }

    @Test
    @DisplayName("Should create booking with multiple seats successfully")
    void shouldCreateBookingSuccessfully() {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .userId(testUser.getId())
                .sessionId(testSession.getId())
                .seatIds(testSeats.stream().map(Seat::getId).toList())
                .build();
        BookingResponse response = bookingService.createBooking(request);
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.getSeats()).hasSize(3);
        assertThat(response.getTotalPrice()).isGreaterThan(BigDecimal.ZERO);
        assertThat(response.getTicketCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should rollback transaction when seat is already booked")
    void shouldRollbackWhenSeatAlreadyBooked() {
        CreateBookingRequest firstRequest = CreateBookingRequest.builder()
                .userId(testUser.getId())
                .sessionId(testSession.getId())
                .seatIds(List.of(testSeats.get(0).getId()))
                .build();
        bookingService.createBooking(firstRequest);
        CreateBookingRequest secondRequest = CreateBookingRequest.builder()
                .userId(testUser.getId())
                .sessionId(testSession.getId())
                .seatIds(List.of(testSeats.get(0).getId()))
                .build();
        assertThatThrownBy(() -> bookingService.createBooking(secondRequest))
                .isInstanceOf(SeatAlreadyBookedException.class)
                .satisfies(ex -> {
                    SeatAlreadyBookedException e = (SeatAlreadyBookedException) ex;
                    assertThat(e.getBookedSeatIds()).contains(testSeats.get(0).getId());
                });
    }

    @Test
    @DisplayName("Should reject booking for expired session")
    void shouldRejectBookingForExpiredSession() {
        Movie movie = movieRepository.findAll().stream().findFirst().orElseThrow();
        Hall hall = hallRepository.findAll().stream().findFirst().orElseThrow();
        Session pastSession = sessionRepository.save(Session.builder()
                .movie(movie)
                .hall(hall)
                .startTime(OffsetDateTime.now().minusHours(2))
                .endTime(OffsetDateTime.now().minusHours(1))
                .basePrice(new BigDecimal("10.00"))
                .status(SessionStatus.COMPLETED)
                .build());
        CreateBookingRequest request = CreateBookingRequest.builder()
                .userId(testUser.getId())
                .sessionId(pastSession.getId())
                .seatIds(List.of(testSeats.get(0).getId()))
                .build();
        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(SessionNotAvailableException.class);
    }

    @Test
    @DisplayName("Should transition booking status correctly")
    void shouldTransitionBookingStatusCorrectly() {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .userId(testUser.getId())
                .sessionId(testSession.getId())
                .seatIds(List.of(testSeats.get(0).getId()))
                .build();
        BookingResponse booking = bookingService.createBooking(request);
        BookingResponse confirmed = bookingService.confirmBooking(booking.getId());
        assertThat(confirmed.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        BookingResponse completed = bookingService.updateBookingStatus(
                booking.getId(),
                UpdateBookingStatusRequest.builder()
                        .newStatus(BookingStatus.COMPLETED)
                        .build());
        assertThat(completed.getStatus()).isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should reject invalid status transition")
    void shouldRejectInvalidStatusTransition() {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .userId(testUser.getId())
                .sessionId(testSession.getId())
                .seatIds(List.of(testSeats.get(0).getId()))
                .build();
        BookingResponse booking = bookingService.createBooking(request);
        assertThatThrownBy(() -> bookingService.updateBookingStatus(
                booking.getId(),
                UpdateBookingStatusRequest.builder()
                        .newStatus(BookingStatus.COMPLETED)
                        .build()))
                .isInstanceOf(InvalidBookingStateException.class);
    }

    @Test
    @DisplayName("Should cancel booking successfully")
    void shouldCancelBookingSuccessfully() {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .userId(testUser.getId())
                .sessionId(testSession.getId())
                .seatIds(List.of(testSeats.get(0).getId()))
                .build();
        BookingResponse booking = bookingService.createBooking(request);
        bookingService.cancelBooking(booking.getId());
        BookingResponse cancelled = bookingService.getBookingById(booking.getId());
        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should handle concurrent booking attempts with pessimistic locking")
    void shouldHandleConcurrentBookingsWithPessimisticLocking() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        Long seatId = testSeats.get(0).getId();
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    CreateBookingRequest request = CreateBookingRequest.builder()
                            .userId(testUser.getId())
                            .sessionId(testSession.getId())
                            .seatIds(List.of(seatId))
                            .build();
                    bookingService.createBooking(request);
                    successCount.incrementAndGet();
                } catch (SeatAlreadyBookedException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
    }

    @Test
    @DisplayName("Should calculate total price correctly with seat multipliers")
    void shouldCalculateTotalPriceCorrectly() {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .userId(testUser.getId())
                .sessionId(testSession.getId())
                .seatIds(testSeats.stream().map(Seat::getId).toList())
                .build();
        BookingResponse response = bookingService.createBooking(request);
        BigDecimal expectedTotal = testSeats.stream()
                .map(seat -> testSession.getBasePrice().multiply(seat.getPriceMultiplier()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(response.getTotalPrice()).isEqualByComparingTo(expectedTotal);
    }
}
