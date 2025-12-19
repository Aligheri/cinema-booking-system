package org.example.db_project.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.db_project.domain.enums.SessionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;
    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;
    @Column(name = "end_time", nullable = false)
    private OffsetDateTime endTime;
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        if (status == null) {
            status = SessionStatus.SCHEDULED;
        }
    }

    public boolean isAvailableForBooking() {
        return status == SessionStatus.SCHEDULED && startTime.isAfter(OffsetDateTime.now());
    }

    public boolean hasStarted() {
        return OffsetDateTime.now().isAfter(startTime);
    }
}
