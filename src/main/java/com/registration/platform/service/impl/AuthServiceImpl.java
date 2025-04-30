package com.registration.platform.service.impl;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager; // Import Auth Manager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Import Auth Token
import org.springframework.security.core.Authentication; // Import Authentication
import org.springframework.security.core.context.SecurityContextHolder; // Import Security Context
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.registration.platform.model.dto.AuthResponse; // Import AuthResponse DTO
import com.registration.platform.model.dto.LoginRequest; // Import LoginRequest DTO
import com.registration.platform.model.dto.RegisterRequest;
import com.registration.platform.model.entity.AuthProvider;
import com.registration.platform.model.entity.Role;
import com.registration.platform.model.entity.User;
import com.registration.platform.repository.UserRepository;
import com.registration.platform.security.JwtTokenProvider; // Import JWT provider
import com.registration.platform.service.AuthService;
import com.registration.platform.service.EmailService; // Import EmailService

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager; // Inject Auth Manager
    private final JwtTokenProvider jwtTokenProvider; // Inject JWT Provider
    private final EmailService emailService; // Inject EmailService

    @Override
    @Transactional // Ensure the operation is atomic
    public User registerUser(RegisterRequest registerRequest) {
        log.info("Attempting to register user with email: {}", registerRequest.getEmail());

        // 1. Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", registerRequest.getEmail());
            // Consider using a custom exception for better error handling upstream
            throw new IllegalArgumentException("Error: Email address is already taken!");
        }

        // 2. Create new user entity
        User user = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword())) // Encode password
                .roles(Set.of(Role.ROLE_APPLICANT)) // Default role for new registrations
                .provider(AuthProvider.LOCAL) // Registered via local form
                .enabled(true) // User is enabled by default (consider email verification later)
                .locked(false)
                .build();

        // 3. Save user to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // 4. Send welcome email asynchronously
        try {
            String subject = "Welcome to the Registration Platform!";
            String text = String.format("Hello %s,\n\nWelcome! Your registration was successful.\n\nThank you,\nThe Platform Team",
                                        savedUser.getFirstName());
            emailService.sendSimpleMessage(savedUser.getEmail(), subject, text);
            log.info("Welcome email queued for user ID: {}", savedUser.getId());
        } catch (Exception e) {
            // Log error but don't fail the registration process if email fails
            log.error("Failed to send welcome email to user ID {}: {}", savedUser.getId(), e.getMessage());
        }

        return savedUser;
    }

    @Override
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        log.info("Attempting to authenticate user with email: {}", loginRequest.getEmail());

        // 1. Perform authentication using Spring Security's AuthenticationManager
        // This will use our CustomUserDetailsService and PasswordEncoder implicitly
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        // 2. If authentication is successful, set the Authentication object in the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("User authenticated successfully: {}", loginRequest.getEmail());

        // 3. Generate JWT token
        String jwt = jwtTokenProvider.generateToken(authentication);
        log.debug("Generated JWT token for user: {}", loginRequest.getEmail());

        // 4. Return the token in an AuthResponse DTO
        return AuthResponse.builder()
                .accessToken(jwt)
                .build();
        // AuthenticationException will be thrown by authenticationManager.authenticate()
        // if credentials are invalid, which can be handled by a global exception handler (@ControllerAdvice)
    }
}