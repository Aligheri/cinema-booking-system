package org.example.db_project.domain.repository;

import org.example.db_project.domain.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    @Query("""
            SELECT CASE WHEN COUNT(bs) > 0 THEN true ELSE false END
            FROM BookingSeat bs
            JOIN bs.booking b
            WHERE bs.seat.id = :seatId
            AND b.session.id = :sessionId
            AND b.status NOT IN ('CANCELLED', 'EXPIRED')
            """)
    boolean isSeatBookedForSession(
            @Param("seatId") Long seatId,
            @Param("sessionId") Long sessionId);

    @Query("""
            SELECT bs.seat.id FROM BookingSeat bs
            JOIN bs.booking b
            WHERE b.session.id = :sessionId
            AND b.status NOT IN ('CANCELLED', 'EXPIRED')
            """)
    List<Long> findBookedSeatIdsForSession(@Param("sessionId") Long sessionId);
}
