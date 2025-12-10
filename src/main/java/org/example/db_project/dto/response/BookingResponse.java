package org.example.db_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.db_project.domain.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private UserResponse user;
    private SessionResponse session;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private List<BookingSeatResponse> seats;
    private OffsetDateTime createdAt;
    private Integer ticketCount;
}
