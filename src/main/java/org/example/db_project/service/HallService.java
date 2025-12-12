package org.example.db_project.service;

import lombok.RequiredArgsConstructor;
import org.example.db_project.domain.entity.Hall;
import org.example.db_project.domain.repository.HallRepository;
import org.example.db_project.dto.response.HallResponse;
import org.example.db_project.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HallService {
    private final HallRepository hallRepository;

    public List<HallResponse> getAllHalls() {
        return hallRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public HallResponse getHallById(Long id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall", id));
        return toResponse(hall);
    }

    public List<HallResponse> getAvailableHalls(OffsetDateTime startTime, OffsetDateTime endTime) {
        return hallRepository.findAvailableHalls(startTime, endTime)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public HallResponse toResponse(Hall hall) {
        return HallResponse.builder()
                .id(hall.getId())
                .name(hall.getName())
                .hallType(hall.getHallType())
                .capacity(hall.getCapacity())
                .build();
    }
}
