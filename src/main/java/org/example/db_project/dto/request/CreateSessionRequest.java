package org.example.db_project.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {
    @NotNull(message = "Movie ID is required")
    private Long movieId;
    @NotNull(message = "Hall ID is required")
    private Long hallId;
    @NotNull(message = "Start time is required")
    private OffsetDateTime startTime;
    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;
}
