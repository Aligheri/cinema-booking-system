package org.example.db_project.service;

import lombok.RequiredArgsConstructor;
import org.example.db_project.domain.entity.Genre;
import org.example.db_project.domain.repository.GenreRepository;
import org.example.db_project.dto.response.GenreResponse;
import org.example.db_project.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public List<GenreResponse> getAllGenres() {
        return genreRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public GenreResponse getGenreById(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", id));
        return toResponse(genre);
    }

    public List<Genre> getGenresByIds(List<Long> ids) {
        List<Genre> genres = genreRepository.findAllById(ids);
        if (genres.size() != ids.size()) {
            throw new ResourceNotFoundException("Some genres not found");
        }
        return genres;
    }

    public GenreResponse toResponse(Genre genre) {
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .description(genre.getDescription())
                .build();
    }
}
