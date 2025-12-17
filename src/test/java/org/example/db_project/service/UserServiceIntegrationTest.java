package org.example.db_project.service;

import org.example.db_project.BaseIntegrationTest;
import org.example.db_project.domain.repository.UserRepository;
import org.example.db_project.dto.request.CreateUserRequest;
import org.example.db_project.dto.response.UserResponse;
import org.example.db_project.exception.DuplicateResourceException;
import org.example.db_project.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

class UserServiceIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        String uniqueEmail = "newuser" + System.currentTimeMillis() + "@test.com";
        CreateUserRequest request = CreateUserRequest.builder()
                .email(uniqueEmail)
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();
        UserResponse response = userService.createUser(request);
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getEmail()).isEqualTo(uniqueEmail);
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getFullName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should reject duplicate email")
    void shouldRejectDuplicateEmail() {
        String email = "duplicate" + System.currentTimeMillis() + "@test.com";
        CreateUserRequest request = CreateUserRequest.builder()
                .email(email)
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();
        userService.createUser(request);
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Should soft delete user")
    void shouldSoftDeleteUser() {
        String email = "todelete" + System.currentTimeMillis() + "@test.com";
        CreateUserRequest request = CreateUserRequest.builder()
                .email(email)
                .password("password123")
                .firstName("To")
                .lastName("Delete")
                .build();
        UserResponse user = userService.createUser(request);
        userService.softDeleteUser(user.getId());
        assertThatThrownBy(() -> userService.getUserById(user.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get user by email")
    void shouldGetUserByEmail() {
        String adminEmail = "admin@cinema.com";
        UserResponse response = userService.getUserByEmail(adminEmail);
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(adminEmail);
    }
}
