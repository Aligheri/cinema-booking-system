package org.example.db_project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.db_project.dto.request.CreateSessionRequest;
import org.example.db_project.dto.response.SeatResponse;
import org.example.db_project.dto.response.SessionResponse;
import org.example.db_project.service.SeatService;
import org.example.db_project.service.SessionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;
    private final SeatService seatService;

    @PostMapping
    public ResponseEntity<SessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        SessionResponse session = sessionService.createSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    @GetMapping("/schedule")
    public ResponseEntity<List<SessionResponse>> getScheduleForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(sessionService.getScheduleForDate(date));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<SessionResponse>> getUpcomingSessionsForMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(sessionService.getUpcomingSessionsForMovie(movieId));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatResponse>> getAvailableSeats(@PathVariable Long id) {
        SessionResponse session = sessionService.getSessionById(id);
        return ResponseEntity.ok(seatService.getAvailableSeatsForSession(
                session.getHall().getId(), id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelSession(@PathVariable Long id) {
        sessionService.cancelSession(id);
        return ResponseEntity.noContent().build();
    }
}
