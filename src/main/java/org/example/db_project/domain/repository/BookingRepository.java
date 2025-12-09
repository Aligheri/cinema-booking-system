package org.example.db_project.domain.repository;

import org.example.db_project.domain.entity.Booking;
import org.example.db_project.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.session s
            JOIN FETCH s.movie
            JOIN FETCH b.bookingSeats bs
            JOIN FETCH bs.seat
            WHERE b.id = :id
            """)
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdWithOptimisticLock(@Param("id") Long id);

    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.session s
            JOIN FETCH s.movie m
            WHERE b.user.id = :userId
            ORDER BY b.createdAt DESC
            """)
    Page<Booking> findUserBookingsWithDetails(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.status = 'PENDING'
            AND b.createdAt < :expirationTime
            """)
    List<Booking> findExpiredPendingBookings(@Param("expirationTime") OffsetDateTime expirationTime);

    @Modifying
    @Query("UPDATE Booking b SET b.status = 'EXPIRED' WHERE b.id IN :ids")
    int expireBookings(@Param("ids") List<Long> ids);
}
