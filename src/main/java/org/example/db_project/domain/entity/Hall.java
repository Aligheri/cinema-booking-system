package org.example.db_project.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.db_project.domain.enums.HallType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "halls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "hall_type", nullable = false, length = 20)
    private HallType hallType;
    @Column(nullable = false)
    private Integer capacity;
    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Seat> seats = new ArrayList<>();
    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Session> sessions = new ArrayList<>();

    public void addSeat(Seat seat) {
        seats.add(seat);
        seat.setHall(this);
    }

    public void removeSeat(Seat seat) {
        seats.remove(seat);
        seat.setHall(null);
    }
}
