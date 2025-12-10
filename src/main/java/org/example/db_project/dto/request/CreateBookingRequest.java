package org.example.db_project.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    @NotNull(message = "Session ID is required")
    private Long sessionId;
    @NotEmpty(message = "At least one seat must be selected")
    private List<Long> seatIds;
}
