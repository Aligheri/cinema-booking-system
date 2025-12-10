package org.example.db_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieRevenueResponse {
    private Long movieId;
    private String title;
    private String genres;
    private BigDecimal revenue;
    private Long bookingsCount;
    private Long ticketsSold;
    private BigDecimal avgTicketPrice;
}
