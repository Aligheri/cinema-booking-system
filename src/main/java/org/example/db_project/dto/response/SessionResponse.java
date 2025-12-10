package org.example.db_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.db_project.domain.enums.SessionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private Long id;
    private MovieResponse movie;
    private HallResponse hall;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private BigDecimal basePrice;
    private SessionStatus status;
    private Integer availableSeats;
}
