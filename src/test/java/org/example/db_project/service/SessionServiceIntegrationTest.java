package org.example.db_project.service;

import org.example.db_project.BaseIntegrationTest;
import org.example.db_project.domain.entity.Hall;
import org.example.db_project.domain.entity.Movie;
import org.example.db_project.domain.entity.Session;
import org.example.db_project.domain.enums.SessionStatus;
import org.example.db_project.domain.repository.HallRepository;
import org.example.db_project.domain.repository.MovieRepository;
import org.example.db_project.domain.repository.SessionRepository;
import org.example.db_project.dto.request.CreateSessionRequest;
import org.example.db_project.dto.response.SessionResponse;
import org.example.db_project.exception.SessionOverlapException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SessionServiceIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private SessionService sessionService;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private HallRepository hallRepository;
    private Movie testMovie;
    private Hall testHall;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
        testMovie = movieRepository.findAll().stream().findFirst().orElseThrow();
        testHall = hallRepository.findAll().stream().findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Should create session successfully")
    void shouldCreateSessionSuccessfully() {
        CreateSessionRequest request = CreateSessionRequest.builder()
                .movieId(testMovie.getId())
                .hallId(testHall.getId())
                .startTime(OffsetDateTime.now().plusDays(1))
                .basePrice(new BigDecimal("15.00"))
                .build();
        SessionResponse response = sessionService.createSession(request);
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(SessionStatus.SCHEDULED);
        assertThat(response.getBasePrice()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(response.getMovie().getId()).isEqualTo(testMovie.getId());
        assertThat(response.getHall().getId()).isEqualTo(testHall.getId());
    }

    @Test
    @DisplayName("Should reject overlapping sessions in same hall")
    void shouldRejectOverlappingSessions() {
        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1).withHour(14).withMinute(0);
        CreateSessionRequest firstRequest = CreateSessionRequest.builder()
                .movieId(testMovie.getId())
                .hallId(testHall.getId())
                .startTime(startTime)
                .basePrice(new BigDecimal("15.00"))
                .build();
        sessionService.createSession(firstRequest);
        CreateSessionRequest overlappingRequest = CreateSessionRequest.builder()
                .movieId(testMovie.getId())
                .hallId(testHall.getId())
                .startTime(startTime.plusMinutes(30))
                .basePrice(new BigDecimal("15.00"))
                .build();
        assertThatThrownBy(() -> sessionService.createSession(overlappingRequest))
                .isInstanceOf(SessionOverlapException.class);
    }

    @Test
    @DisplayName("Should get schedule for date")
    void shouldGetScheduleForDate() {
        OffsetDateTime tomorrow = OffsetDateTime.now().plusDays(1).withHour(10).withMinute(0);
        CreateSessionRequest request = CreateSessionRequest.builder()
                .movieId(testMovie.getId())
                .hallId(testHall.getId())
                .startTime(tomorrow)
                .basePrice(new BigDecimal("12.00"))
                .build();
        sessionService.createSession(request);
        List<SessionResponse> schedule = sessionService.getScheduleForDate(tomorrow.toLocalDate());
        assertThat(schedule).isNotEmpty();
    }

    @Test
    @DisplayName("Should cancel session successfully")
    void shouldCancelSession() {
        CreateSessionRequest request = CreateSessionRequest.builder()
                .movieId(testMovie.getId())
                .hallId(testHall.getId())
                .startTime(OffsetDateTime.now().plusDays(2))
                .basePrice(new BigDecimal("15.00"))
                .build();
        SessionResponse session = sessionService.createSession(request);
        sessionService.cancelSession(session.getId());
        SessionResponse cancelled = sessionService.getSessionById(session.getId());
        assertThat(cancelled.getStatus()).isEqualTo(SessionStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should reject cancelling started session")
    void shouldRejectCancellingStartedSession() {
        Session pastSession = sessionRepository.save(Session.builder()
                .movie(testMovie)
                .hall(testHall)
                .startTime(OffsetDateTime.now().minusHours(1))
                .endTime(OffsetDateTime.now().plusHours(1))
                .basePrice(new BigDecimal("15.00"))
                .status(SessionStatus.ONGOING)
                .build());
        assertThatThrownBy(() -> sessionService.cancelSession(pastSession.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already started");
    }
}
