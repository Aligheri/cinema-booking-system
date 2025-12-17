package org.example.db_project.service;

import org.example.db_project.BaseIntegrationTest;
import org.example.db_project.domain.entity.*;
import org.example.db_project.domain.enums.*;
import org.example.db_project.domain.repository.*;
import org.example.db_project.dto.response.MovieRevenueResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class AnalyticsServiceIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private AnalyticsService analyticsService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private BookingSeatRepository bookingSeatRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private HallRepository hallRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        sessionRepository.deleteAll();
        User user = userRepository.findByEmail("admin@cinema.com").orElseThrow();
        Movie movie = movieRepository.findAll().stream().findFirst().orElseThrow();
        Hall hall = hallRepository.findAll().stream().findFirst().orElseThrow();
        List<Seat> seats = seatRepository.findByHallIdOrdered(hall.getId()).subList(0, 2);
        Session session = sessionRepository.save(Session.builder()
                .movie(movie)
                .hall(hall)
                .startTime(OffsetDateTime.now().minusDays(1))
                .endTime(OffsetDateTime.now().minusDays(1).plusHours(2))
                .basePrice(new BigDecimal("10.00"))
                .status(SessionStatus.COMPLETED)
                .build());
        Booking booking = Booking.builder()
                .user(user)
                .session(session)
                .totalPrice(new BigDecimal("20.00"))
                .status(BookingStatus.COMPLETED)
                .build();
        for (Seat seat : seats) {
            BookingSeat bookingSeat = BookingSeat.builder()
                    .seat(seat)
                    .price(new BigDecimal("10.00"))
                    .build();
            booking.addBookingSeat(bookingSeat);
        }
        bookingRepository.save(booking);
    }

    @Test
    @DisplayName("Should get top movies by revenue")
    void shouldGetTopMoviesByRevenue() {
        List<MovieRevenueResponse> results = analyticsService.getTopMoviesByRevenue(5);
        assertThat(results).isNotEmpty();
        MovieRevenueResponse topMovie = results.stream()
                .filter(r -> r.getRevenue().compareTo(BigDecimal.ZERO) > 0)
                .findFirst()
                .orElse(null);
        if (topMovie != null) {
            assertThat(topMovie.getTitle()).isNotBlank();
            assertThat(topMovie.getRevenue()).isGreaterThan(BigDecimal.ZERO);
        }
    }

    @Test
    @DisplayName("Should get session occupancy stats")
    void shouldGetSessionOccupancyStats() {
        Movie movie = movieRepository.findAll().stream().findFirst().orElseThrow();
        Hall hall = hallRepository.findAll().stream().findFirst().orElseThrow();
        sessionRepository.save(Session.builder()
                .movie(movie)
                .hall(hall)
                .startTime(OffsetDateTime.now().plusDays(1))
                .endTime(OffsetDateTime.now().plusDays(1).plusHours(2))
                .basePrice(new BigDecimal("15.00"))
                .status(SessionStatus.SCHEDULED)
                .build());
        List<Object[]> results = analyticsService.getSessionOccupancyStats();
        assertThat(results).isNotEmpty();
    }

    @Test
    @DisplayName("Should handle empty results gracefully")
    void shouldHandleEmptyResultsGracefully() {
        bookingRepository.deleteAll();
        List<MovieRevenueResponse> results = analyticsService.getTopMoviesByRevenue(5);
        assertThat(results).isNotEmpty();
    }
}
