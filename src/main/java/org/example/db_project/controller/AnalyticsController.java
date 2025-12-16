package org.example.db_project.controller;

import lombok.RequiredArgsConstructor;
import org.example.db_project.dto.response.MovieRevenueResponse;
import org.example.db_project.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/movies/revenue")
    public ResponseEntity<List<MovieRevenueResponse>> getTopMoviesByRevenue(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopMoviesByRevenue(limit));
    }

    @GetMapping("/sessions/occupancy")
    public ResponseEntity<List<Object[]>> getSessionOccupancyStats() {
        return ResponseEntity.ok(analyticsService.getSessionOccupancyStats());
    }

    @GetMapping("/revenue/by-hall-type")
    public ResponseEntity<List<Object[]>> getRevenueByHallType() {
        return ResponseEntity.ok(analyticsService.getRevenueByHallType());
    }

    @GetMapping("/revenue/daily")
    public ResponseEntity<List<Object[]>> getDailyRevenue(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(analyticsService.getDailyRevenue(days));
    }
}
