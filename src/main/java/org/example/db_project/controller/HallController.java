package org.example.db_project.controller;

import lombok.RequiredArgsConstructor;
import org.example.db_project.dto.response.HallResponse;
import org.example.db_project.dto.response.SeatResponse;
import org.example.db_project.service.HallService;
import org.example.db_project.service.SeatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class HallController {
    private final HallService hallService;
    private final SeatService seatService;

    @GetMapping
    public ResponseEntity<List<HallResponse>> getAllHalls() {
        return ResponseEntity.ok(hallService.getAllHalls());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HallResponse> getHallById(@PathVariable Long id) {
        return ResponseEntity.ok(hallService.getHallById(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<HallResponse>> getAvailableHalls(
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime) {
        return ResponseEntity.ok(hallService.getAvailableHalls(startTime, endTime));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatResponse>> getHallSeats(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.getSeatsByHall(id));
    }
}
