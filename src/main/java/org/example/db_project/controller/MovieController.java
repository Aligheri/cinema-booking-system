package org.example.db_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.db_project.dto.request.CreateMovieRequest;
import org.example.db_project.dto.response.MovieResponse;
import org.example.db_project.service.MovieService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Tag(name = "Movies", description = "Movie catalog management - CRUD operations, search, and filtering")
public class MovieController {
    private final MovieService movieService;

    @PostMapping
    @Operation(summary = "Create a new movie", description = "Add a new movie to the catalog with genres")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Movie created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody CreateMovieRequest request) {
        MovieResponse movie = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(movie);
    }

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> searchMovies(@RequestParam String title) {
        return ResponseEntity.ok(movieService.searchMovies(title));
    }

    @GetMapping("/search/fulltext")
    @Operation(summary = "Full-text search", description = "Search movies using PostgreSQL full-text search with GIN index")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<List<MovieResponse>> fullTextSearch(
            @Parameter(description = "Search query (searches in title and description)", example = "action hero")
            @RequestParam String q) {
        return ResponseEntity.ok(movieService.fullTextSearch(q));
    }

    @GetMapping("/genre/{genreId}")
    public ResponseEntity<List<MovieResponse>> getMoviesByGenre(@PathVariable Long genreId) {
        return ResponseEntity.ok(movieService.getMoviesByGenre(genreId));
    }

    @GetMapping("/now-showing")
    public ResponseEntity<Page<MovieResponse>> getCurrentlyShowingMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(movieService.getCurrentlyShowingMovies(page, size));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<MovieResponse>> getUpcomingMovies() {
        return ResponseEntity.ok(movieService.getUpcomingMovies());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.softDeleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}
