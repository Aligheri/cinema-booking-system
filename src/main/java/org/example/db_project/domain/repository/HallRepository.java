package org.example.db_project.domain.repository;

import org.example.db_project.domain.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {

    @Query(value = """
            SELECT h.* FROM halls h
            WHERE h.id NOT IN (
                SELECT DISTINCT s.hall_id FROM sessions s
                WHERE s.start_time <= :endTime AND s.end_time >= :startTime
                AND s.status != 'CANCELLED'
            )
            """, nativeQuery = true)
    List<Hall> findAvailableHalls(
            @Param("startTime") java.time.OffsetDateTime startTime,
            @Param("endTime") java.time.OffsetDateTime endTime);
}
