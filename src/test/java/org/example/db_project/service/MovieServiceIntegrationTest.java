package org.example.db_project.service;

import org.example.db_project.BaseIntegrationTest;
import org.example.db_project.domain.repository.GenreRepository;
import org.example.db_project.domain.repository.MovieRepository;
import org.example.db_project.dto.request.CreateMovieRequest;
import org.example.db_project.dto.response.MovieResponse;
import org.example.db_project.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class MovieServiceIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MovieService movieService;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private GenreRepository genreRepository;

    @Test
    @DisplayName("Should create movie with genres successfully")
    void shouldCreateMovieWithGenres() {
        List<Long> genreIds = genreRepository.findAll().stream()
                .limit(2)
                .map(g -> g.getId())
                .toList();
        CreateMovieRequest request = CreateMovieRequest.builder()
                .title("Test Movie " + System.currentTimeMillis())
                .description("A test movie description")
                .durationMinutes(120)
                .releaseDate(LocalDate.now())
                .rating(new BigDecimal("8.5"))
                .posterUrl("/posters/test.jpg")
                .genreIds(genreIds)
                .build();
        MovieResponse response = movieService.createMovie(request);
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        assertThat(response.getDurationMinutes()).isEqualTo(120);
        assertThat(response.getGenres()).hasSize(2);
    }

    @Test
    @DisplayName("Should search movies by title")
    void shouldSearchMoviesByTitle() {
        List<MovieResponse> results = movieService.searchMovies("Matrix");
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).containsIgnoringCase("Matrix");
    }

    @Test
    @DisplayName("Should get movies by genre")
    void shouldGetMoviesByGenre() {
        Long actionGenreId = genreRepository.findByName("Action")
                .orElseThrow()
                .getId();
        List<MovieResponse> movies = movieService.getMoviesByGenre(actionGenreId);
        assertThat(movies).isNotEmpty();
    }

    @Test
    @DisplayName("Should soft delete movie")
    void shouldSoftDeleteMovie() {
        List<Long> genreIds = genreRepository.findAll().stream()
                .limit(1)
                .map(g -> g.getId())
                .toList();
        CreateMovieRequest request = CreateMovieRequest.builder()
                .title("To Delete " + System.currentTimeMillis())
                .description("Will be deleted")
                .durationMinutes(90)
                .genreIds(genreIds)
                .build();
        MovieResponse movie = movieService.createMovie(request);
        movieService.softDeleteMovie(movie.getId());
        assertThatThrownBy(() -> movieService.getMovieById(movie.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get currently showing movies with pagination")
    void shouldGetCurrentlyShowingMoviesWithPagination() {
        Page<MovieResponse> page = movieService.getCurrentlyShowingMovies(0, 10);
        assertThat(page).isNotNull();
        assertThat(page.getContent()).isNotEmpty();
    }
}
