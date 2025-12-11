package org.example.db_project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.db_project.domain.entity.User;
import org.example.db_project.domain.repository.UserRepository;
import org.example.db_project.dto.request.CreateUserRequest;
import org.example.db_project.dto.response.UserResponse;
import org.example.db_project.exception.DuplicateResourceException;
import org.example.db_project.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(hashPassword(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
        user = userRepository.save(user);
        log.info("User created with id: {}", user.getId());
        return toResponse(user);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return toResponse(user);
    }

    public List<UserResponse> getAllUsers(int page, int size) {
        return userRepository.findAllWithPagination(size, page * size)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void softDeleteUser(Long id) {
        log.info("Soft deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.softDelete(id);
        log.info("User soft deleted: {}", id);
    }

    private String hashPassword(String password) {
        return "$2a$10$" + password.hashCode();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
