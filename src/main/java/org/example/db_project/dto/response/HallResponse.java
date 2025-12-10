package org.example.db_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.db_project.domain.enums.HallType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HallResponse {
    private Long id;
    private String name;
    private HallType hallType;
    private Integer capacity;
}
