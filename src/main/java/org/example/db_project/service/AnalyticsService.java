package org.example.db_project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.db_project.dto.response.MovieRevenueResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<MovieRevenueResponse> getTopMoviesByRevenue(int limit) {
        log.info("Fetching top {} movies by revenue for current month", limit);
        String sql = """
                WITH monthly_revenue AS (
                    SELECT
                        m.id as movie_id,
                        m.title,
                        COALESCE(SUM(b.total_price), 0) as revenue,
                        COUNT(DISTINCT b.id) as bookings_count,
                        COUNT(bs.id) as tickets_sold
                    FROM movies m
                    LEFT JOIN sessions s ON m.id = s.movie_id
                    LEFT JOIN bookings b ON s.id = b.session_id AND b.status = 'COMPLETED'
                    LEFT JOIN booking_seats bs ON b.id = bs.booking_id
                    WHERE m.deleted_at IS NULL
                    AND (b.created_at IS NULL OR b.created_at >= DATE_TRUNC('month', CURRENT_DATE))
                    GROUP BY m.id, m.title
                ),
                genre_stats AS (
                    SELECT
                        mr.movie_id,
                        STRING_AGG(g.name, ', ' ORDER BY g.name) as genres
                    FROM monthly_revenue mr
                    LEFT JOIN movie_genres mg ON mr.movie_id = mg.movie_id
                    LEFT JOIN genres g ON mg.genre_id = g.id
                    GROUP BY mr.movie_id
                )
                SELECT
                    mr.movie_id,
                    mr.title,
                    COALESCE(gs.genres, 'No genres') as genres,
                    mr.revenue,
                    mr.bookings_count,
                    mr.tickets_sold
                FROM monthly_revenue mr
                LEFT JOIN genre_stats gs ON mr.movie_id = gs.movie_id
                ORDER BY mr.revenue DESC
                LIMIT :limit
                """;
        List<Object[]> results = entityManager.createNativeQuery(sql)
                .setParameter("limit", limit)
                .getResultList();
        return results.stream()
                .map(row -> {
                    BigDecimal revenue = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
                    Long ticketsSold = row[5] != null ? ((Number) row[5]).longValue() : 0L;
                    BigDecimal avgPrice = ticketsSold > 0
                            ? revenue.divide(BigDecimal.valueOf(ticketsSold), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return MovieRevenueResponse.builder()
                            .movieId(((Number) row[0]).longValue())
                            .title((String) row[1])
                            .genres((String) row[2])
                            .revenue(revenue)
                            .bookingsCount(((Number) row[4]).longValue())
                            .ticketsSold(ticketsSold)
                            .avgTicketPrice(avgPrice)
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Object[]> getSessionOccupancyStats() {
        String sql = """
                SELECT
                    s.id as session_id,
                    m.title as movie_title,
                    h.name as hall_name,
                    h.capacity,
                    COUNT(DISTINCT bs.seat_id) as booked_seats,
                    ROUND(COUNT(DISTINCT bs.seat_id)::numeric / h.capacity * 100, 2) as occupancy_percent
                FROM sessions s
                JOIN movies m ON s.movie_id = m.id
                JOIN halls h ON s.hall_id = h.id
                LEFT JOIN bookings b ON s.id = b.session_id AND b.status NOT IN ('CANCELLED', 'EXPIRED')
                LEFT JOIN booking_seats bs ON b.id = bs.booking_id
                WHERE s.status = 'SCHEDULED'
                AND s.start_time > CURRENT_TIMESTAMP
                GROUP BY s.id, m.title, h.name, h.capacity
                ORDER BY s.start_time
                """;
        return entityManager.createNativeQuery(sql).getResultList();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Object[]> getRevenueByHallType() {
        String sql = """
                SELECT
                    h.hall_type,
                    COUNT(DISTINCT b.id) as total_bookings,
                    SUM(b.total_price) as total_revenue,
                    AVG(b.total_price) as avg_booking_value
                FROM halls h
                JOIN sessions s ON h.id = s.hall_id
                JOIN bookings b ON s.id = b.session_id
                WHERE b.status = 'COMPLETED'
                GROUP BY h.hall_type
                ORDER BY total_revenue DESC
                """;
        return entityManager.createNativeQuery(sql).getResultList();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Object[]> getDailyRevenue(int days) {
        String sql = """
                SELECT
                    DATE(b.created_at) as booking_date,
                    COUNT(DISTINCT b.id) as bookings_count,
                    SUM(b.total_price) as daily_revenue,
                    COUNT(bs.id) as tickets_sold
                FROM bookings b
                JOIN booking_seats bs ON b.id = bs.booking_id
                WHERE b.status = 'COMPLETED'
                AND b.created_at >= CURRENT_DATE - INTERVAL '%d days'
                GROUP BY DATE(b.created_at)
                ORDER BY booking_date DESC
                """.formatted(days);
        return entityManager.createNativeQuery(sql).getResultList();
    }
}
