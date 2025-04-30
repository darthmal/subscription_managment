package com.registration.platform.config;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.registration.platform.model.entity.AuthProvider;
import com.registration.platform.model.entity.Role;
import com.registration.platform.model.entity.User;
import com.registration.platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@example.com}") // Default admin email from properties or fallback
    private String adminEmail;

    @Value("${app.admin.password:adminpassword}") // Default admin password from properties or fallback
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        // Check if an admin user already exists
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user with email {} already exists. Skipping creation.", adminEmail);
            return;
        }

        // Create the default admin user
        User adminUser = User.builder()
                .firstName("Admin")
                .lastName("User")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword)) // Encode the password
                .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_APPLICANT)) // Assign ADMIN role (and maybe APPLICANT for testing)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .locked(false)
                .build();

        userRepository.save(adminUser);
        log.info("Default admin user created successfully with email: {}", adminEmail);
        log.warn("Default admin password is set to: '{}'. Please change this in a production environment or via application properties (app.admin.email, app.admin.password).", adminPassword);
    }
}