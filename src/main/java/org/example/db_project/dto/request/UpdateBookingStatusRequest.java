package org.example.db_project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.db_project.domain.enums.BookingStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingStatusRequest {
    @NotNull(message = "New status is required")
    private BookingStatus newStatus;
}
