package org.example.db_project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.db_project.domain.entity.Hall;
import org.example.db_project.domain.entity.Movie;
import org.example.db_project.domain.entity.Session;
import org.example.db_project.domain.enums.SessionStatus;
import org.example.db_project.domain.repository.HallRepository;
import org.example.db_project.domain.repository.MovieRepository;
import org.example.db_project.domain.repository.SessionRepository;
import org.example.db_project.dto.request.CreateSessionRequest;
import org.example.db_project.dto.response.SessionResponse;
import org.example.db_project.exception.ResourceNotFoundException;
import org.example.db_project.exception.SessionOverlapException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {
    private final SessionRepository sessionRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final MovieService movieService;
    private final HallService hallService;
    private final SeatService seatService;

    @Transactional
    public SessionResponse createSession(CreateSessionRequest request) {
        log.info("Creating session for movie {} in hall {}", request.getMovieId(), request.getHallId());
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", request.getMovieId()));
        Hall hall = hallRepository.findById(request.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Hall", request.getHallId()));
        OffsetDateTime endTime = request.getStartTime()
                .plusMinutes(movie.getDurationMinutes())
                .plusMinutes(15);
        List<Session> overlapping = sessionRepository.findOverlappingSessions(
                hall.getId(), request.getStartTime(), endTime);
        if (!overlapping.isEmpty()) {
            throw new SessionOverlapException(hall.getId());
        }
        Session session = Session.builder()
                .movie(movie)
                .hall(hall)
                .startTime(request.getStartTime())
                .endTime(endTime)
                .basePrice(request.getBasePrice())
                .status(SessionStatus.SCHEDULED)
                .build();
        session = sessionRepository.save(session);
        log.info("Session created with id: {}", session.getId());
        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public SessionResponse getSessionById(Long id) {
        Session session = sessionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session", id));
        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getScheduleForDate(LocalDate date) {
        return sessionRepository.findByDateAndStatus(date, SessionStatus.SCHEDULED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SessionResponse> getUpcomingSessionsForMovie(Long movieId) {
        return sessionRepository.findUpcomingSessionsForMovie(movieId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void cancelSession(Long id) {
        log.info("Cancelling session: {}", id);
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session", id));
        if (session.hasStarted()) {
            throw new IllegalStateException("Cannot cancel session that has already started");
        }
        sessionRepository.updateStatus(id, SessionStatus.CANCELLED);
        log.info("Session cancelled: {}", id);
    }

    @Transactional
    public int updateSessionStatuses() {
        int ongoing = sessionRepository.updateOngoingSessions();
        int completed = sessionRepository.updateCompletedSessions();
        log.info("Updated {} sessions to ONGOING, {} to COMPLETED", ongoing, completed);
        return ongoing + completed;
    }

    private SessionResponse toResponse(Session session) {
        int availableSeats = seatService.countAvailableSeats(
                session.getHall().getId(), session.getId());
        return SessionResponse.builder()
                .id(session.getId())
                .movie(MovieService_toSimpleResponse(session.getMovie()))
                .hall(hallService.toResponse(session.getHall()))
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .basePrice(session.getBasePrice())
                .status(session.getStatus())
                .availableSeats(availableSeats)
                .build();
    }

    private org.example.db_project.dto.response.MovieResponse MovieService_toSimpleResponse(Movie movie) {
        return org.example.db_project.dto.response.MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .durationMinutes(movie.getDurationMinutes())
                .rating(movie.getRating())
                .posterUrl(movie.getPosterUrl())
                .build();
    }
}
