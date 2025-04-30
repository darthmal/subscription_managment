package com.registration.platform.repository;

import com.registration.platform.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email (used for login and checking duplicates)
    Optional<User> findByEmail(String email);

    // Check if a user exists by email
    Boolean existsByEmail(String email);

    // Find user by OAuth provider and provider ID
    Optional<User> findByProviderAndProviderId(com.registration.platform.model.entity.AuthProvider provider, String providerId);
}