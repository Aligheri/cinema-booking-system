package org.example.db_project.domain.repository;

import org.example.db_project.domain.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.id = :genreId")
    List<Movie> findByGenreId(@Param("genreId") Long genreId);

    @Query("SELECT DISTINCT m FROM Movie m JOIN FETCH m.genres WHERE m.id = :id")
    Movie findByIdWithGenres(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Movie m SET m.deletedAt = CURRENT_TIMESTAMP WHERE m.id = :id")
    void softDelete(@Param("id") Long id);

    @Query(value = """
            SELECT m.* FROM movies m
            WHERE m.deleted_at IS NULL
            AND to_tsvector('simple', m.title) @@ plainto_tsquery('simple', :searchTerm)
            """, nativeQuery = true)
    List<Movie> fullTextSearch(@Param("searchTerm") String searchTerm);

    @Query("SELECT m FROM Movie m WHERE m.releaseDate > CURRENT_DATE ORDER BY m.releaseDate ASC")
    List<Movie> findUpcomingMovies();

    @Query("SELECT m FROM Movie m WHERE m.releaseDate <= CURRENT_DATE ORDER BY m.releaseDate DESC")
    Page<Movie> findCurrentlyShowingMovies(Pageable pageable);
}
