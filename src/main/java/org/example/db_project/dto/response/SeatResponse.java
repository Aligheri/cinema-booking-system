package org.example.db_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.db_project.domain.enums.SeatType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {
    private Long id;
    private Integer rowNumber;
    private Integer seatNumber;
    private SeatType seatType;
    private BigDecimal priceMultiplier;
    private String seatLabel;
    private boolean available;
}
