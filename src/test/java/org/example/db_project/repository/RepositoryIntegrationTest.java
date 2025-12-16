package org.example.db_project.repository;

import org.example.db_project.BaseIntegrationTest;
import org.example.db_project.domain.entity.*;
import org.example.db_project.domain.enums.*;
import org.example.db_project.domain.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
class RepositoryIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private HallRepository hallRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private SessionRepository sessionRepository;

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        var user = userRepository.findByEmail("admin@cinema.com");
        assertThat(user).isPresent();
        assertThat(user.get().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Should find movies by genre")
    void shouldFindMoviesByGenre() {
        Long actionGenreId = genreRepository.findByName("Action")
                .orElseThrow()
                .getId();
        List<Movie> movies = movieRepository.findByGenreId(actionGenreId);
        assertThat(movies).isNotEmpty();
    }

    @Test
    @DisplayName("Should find seats by hall ordered")
    void shouldFindSeatsByHallOrdered() {
        Long hallId = hallRepository.findAll().stream()
                .findFirst()
                .orElseThrow()
                .getId();
        List<Seat> seats = seatRepository.findByHallIdOrdered(hallId);
        assertThat(seats).isNotEmpty();
        for (int i = 1; i < seats.size(); i++) {
            Seat prev = seats.get(i - 1);
            Seat curr = seats.get(i);
            boolean ordered = prev.getRowNumber() < curr.getRowNumber() ||
                              (prev.getRowNumber().equals(curr.getRowNumber()) &&
                               prev.getSeatNumber() <= curr.getSeatNumber());
            assertThat(ordered).isTrue();
        }
    }

    @Test
    @DisplayName("Should find available halls for time slot")
    void shouldFindAvailableHallsForTimeSlot() {
        OffsetDateTime start = OffsetDateTime.now().plusDays(10);
        OffsetDateTime end = start.plusHours(3);
        List<Hall> halls = hallRepository.findAvailableHalls(start, end);
        assertThat(halls).isNotEmpty();
    }

    @Test
    @DisplayName("Should find overlapping sessions")
    void shouldFindOverlappingSessions() {
        Hall hall = hallRepository.findAll().stream().findFirst().orElseThrow();
        Movie movie = movieRepository.findAll().stream().findFirst().orElseThrow();
        OffsetDateTime sessionStart = OffsetDateTime.now().plusDays(5).withHour(14).withMinute(0).withSecond(0).withNano(0);
        Session session = sessionRepository.save(Session.builder()
                .movie(movie)
                .hall(hall)
                .startTime(sessionStart)
                .endTime(sessionStart.plusHours(2))
                .basePrice(new BigDecimal("10.00"))
                .status(SessionStatus.SCHEDULED)
                .build());
        entityManager.flush();
        entityManager.clear();
        List<Session> overlapping = sessionRepository.findOverlappingSessions(
                hall.getId(),
                sessionStart.plusMinutes(30),
                sessionStart.plusMinutes(60));
        assertThat(overlapping).isNotEmpty();
        assertThat(overlapping).anyMatch(s -> s.getId().equals(session.getId()));
    }

    @Test
    @DisplayName("Should count available seats for session")
    void shouldCountAvailableSeatsForSession() {
        Hall hall = hallRepository.findAll().stream().findFirst().orElseThrow();
        Movie movie = movieRepository.findAll().stream().findFirst().orElseThrow();
        Session session = sessionRepository.save(Session.builder()
                .movie(movie)
                .hall(hall)
                .startTime(OffsetDateTime.now().plusDays(7))
                .endTime(OffsetDateTime.now().plusDays(7).plusHours(2))
                .basePrice(new BigDecimal("10.00"))
                .status(SessionStatus.SCHEDULED)
                .build());
        int availableSeats = seatRepository.countAvailableSeatsForSession(
                hall.getId(), session.getId());
        assertThat(availableSeats).isEqualTo(hall.getCapacity());
    }
}
