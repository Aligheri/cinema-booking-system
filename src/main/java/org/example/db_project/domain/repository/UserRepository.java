package org.example.db_project.domain.repository;

import org.example.db_project.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void softDelete(@Param("id") Long id);

    @Query(value = """
            SELECT u.* FROM users u
            WHERE u.deleted_at IS NULL
            ORDER BY u.created_at DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<User> findAllWithPagination(@Param("limit") int limit, @Param("offset") int offset);
}
