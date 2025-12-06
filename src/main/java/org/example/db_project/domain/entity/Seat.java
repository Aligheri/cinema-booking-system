package org.example.db_project.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.db_project.domain.enums.SeatType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hall_id", "row_number", "seat_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;
    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false, length = 20)
    private SeatType seatType;
    @Column(name = "price_multiplier", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal priceMultiplier = BigDecimal.ONE;
    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL)
    @Builder.Default
    private List<BookingSeat> bookingSeats = new ArrayList<>();

    public String getSeatLabel() {
        return "Row " + rowNumber + ", Seat " + seatNumber;
    }
}
