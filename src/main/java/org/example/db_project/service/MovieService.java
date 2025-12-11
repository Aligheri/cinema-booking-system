package org.example.db_project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.db_project.domain.entity.Genre;
import org.example.db_project.domain.entity.Movie;
import org.example.db_project.domain.repository.MovieRepository;
import org.example.db_project.dto.request.CreateMovieRequest;
import org.example.db_project.dto.response.MovieResponse;
import org.example.db_project.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {
    private final MovieRepository movieRepository;
    private final GenreService genreService;

    @Transactional
    public MovieResponse createMovie(CreateMovieRequest request) {
        log.info("Creating movie: {}", request.getTitle());
        List<Genre> genres = genreService.getGenresByIds(request.getGenreIds());
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .releaseDate(request.getReleaseDate())
                .rating(request.getRating())
                .posterUrl(request.getPosterUrl())
                .build();
        genres.forEach(movie::addGenre);
        movie = movieRepository.save(movie);
        log.info("Movie created with id: {}", movie.getId());
        return toResponse(movie);
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findByIdWithGenres(id);
        if (movie == null) {
            throw new ResourceNotFoundException("Movie", id);
        }
        return toResponse(movie);
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> searchMovies(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getMoviesByGenre(Long genreId) {
        return movieRepository.findByGenreId(genreId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<MovieResponse> getCurrentlyShowingMovies(int page, int size) {
        return movieRepository.findCurrentlyShowingMovies(PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getUpcomingMovies() {
        return movieRepository.findUpcomingMovies()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> fullTextSearch(String searchTerm) {
        return movieRepository.fullTextSearch(searchTerm)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void softDeleteMovie(Long id) {
        log.info("Soft deleting movie with id: {}", id);
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie", id);
        }
        movieRepository.softDelete(id);
        log.info("Movie soft deleted: {}", id);
    }

    private MovieResponse toResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .durationMinutes(movie.getDurationMinutes())
                .releaseDate(movie.getReleaseDate())
                .rating(movie.getRating())
                .posterUrl(movie.getPosterUrl())
                .genres(movie.getGenres().stream()
                        .map(genreService::toResponse)
                        .toList())
                .build();
    }
}
