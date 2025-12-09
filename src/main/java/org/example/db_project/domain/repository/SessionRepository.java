package org.example.db_project.domain.repository;

import org.example.db_project.domain.entity.Session;
import org.example.db_project.domain.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    @Query("""
            SELECT s FROM Session s
            JOIN FETCH s.movie
            JOIN FETCH s.hall
            WHERE s.id = :id
            """)
    Optional<Session> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT s FROM Session s
            JOIN FETCH s.movie m
            JOIN FETCH s.hall h
            WHERE CAST(s.startTime AS date) = :date
            AND s.status = :status
            ORDER BY s.startTime
            """)
    List<Session> findByDateAndStatus(
            @Param("date") LocalDate date,
            @Param("status") SessionStatus status);

    @Query("""
            SELECT s FROM Session s
            WHERE s.movie.id = :movieId
            AND s.startTime > CURRENT_TIMESTAMP
            AND s.status = 'SCHEDULED'
            ORDER BY s.startTime
            """)
    List<Session> findUpcomingSessionsForMovie(@Param("movieId") Long movieId);

    @Query("""
            SELECT s FROM Session s
            WHERE s.hall.id = :hallId
            AND s.startTime < :endTime
            AND s.endTime > :startTime
            AND s.status != 'CANCELLED'
            """)
    List<Session> findOverlappingSessions(
            @Param("hallId") Long hallId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime);

    @Modifying
    @Query("UPDATE Session s SET s.status = :status WHERE s.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") SessionStatus status);

    @Modifying
    @Query("""
            UPDATE Session s SET s.status = 'ONGOING'
            WHERE s.status = 'SCHEDULED'
            AND s.startTime <= CURRENT_TIMESTAMP
            AND s.endTime > CURRENT_TIMESTAMP
            """)
    int updateOngoingSessions();

    @Modifying
    @Query("""
            UPDATE Session s SET s.status = 'COMPLETED'
            WHERE s.status IN ('SCHEDULED', 'ONGOING')
            AND s.endTime <= CURRENT_TIMESTAMP
            """)
    int updateCompletedSessions();
}
