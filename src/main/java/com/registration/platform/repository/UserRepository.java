package com.registration.platform.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.registration.platform.model.entity.ApplicationStatus;
import com.registration.platform.model.entity.Role;
import com.registration.platform.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email (used for login and checking duplicates)
    Optional<User> findByEmail(String email);

    // Check if a user exists by email
    Boolean existsByEmail(String email);

    // Find user by OAuth provider and provider ID
    Optional<User> findByProviderAndProviderId(com.registration.platform.model.entity.AuthProvider provider, String providerId);

    // Count users with a specific role
    long countByRolesContaining(Role role);

    // Count users with a specific application status
    long countByApplicationStatus(ApplicationStatus status);

    // Count users with a specific application status updated within a specific timeframe
    @Query("SELECT COUNT(u) FROM User u WHERE u.applicationStatus = :status AND u.updatedAt >= :since")
    long countByApplicationStatusAndUpdatedAtAfter(@Param("status") ApplicationStatus status, @Param("since") LocalDateTime since);

    // Count users with a specific role updated within a specific timeframe
    // Note: This might be less efficient than counting all and then filtering if the date range is large
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role AND u.updatedAt >= :since")
    long countByRoleAndUpdatedAtAfter(@Param("role") Role role, @Param("since") LocalDateTime since);

    // Count users with a specific role AND status
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role AND u.applicationStatus = :status")
    long countByRoleAndApplicationStatus(@Param("role") Role role, @Param("status") ApplicationStatus status);

}