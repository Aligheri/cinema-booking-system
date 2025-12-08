package org.example.db_project.domain.repository;

import org.example.db_project.domain.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByHallId(Long hallId);

    @Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId ORDER BY s.rowNumber, s.seatNumber")
    List<Seat> findByHallIdOrdered(@Param("hallId") Long hallId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds")
    List<Seat> findByIdsWithLock(@Param("seatIds") List<Long> seatIds);

    @Query(value = """
            SELECT COUNT(*) FROM seats s
            WHERE s.hall_id = :hallId
            AND s.id NOT IN (
                SELECT bs.seat_id FROM booking_seats bs
                JOIN bookings b ON bs.booking_id = b.id
                WHERE b.session_id = :sessionId
                AND b.status NOT IN ('CANCELLED', 'EXPIRED')
            )
            """, nativeQuery = true)
    int countAvailableSeatsForSession(
            @Param("hallId") Long hallId,
            @Param("sessionId") Long sessionId);
}
