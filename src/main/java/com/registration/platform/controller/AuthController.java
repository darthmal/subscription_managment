package com.registration.platform.controller;

import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException; // Import ResponseStatusException

import com.registration.platform.model.dto.AuthResponse;
import com.registration.platform.model.dto.LoginRequest; // Import AuthenticationException
import com.registration.platform.model.dto.RegisterRequest;
import com.registration.platform.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class); // Initialize logger
    private final AuthService authService; // Inject the AuthService

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Received registration request for email: {}", registerRequest.getEmail());
        try {
            // Delegate the registration logic to the AuthService, which now returns AuthResponse
            AuthResponse authResponse = authService.registerUser(registerRequest);
            log.info("User registered successfully: {}", registerRequest.getEmail());
            // Return 201 Created with the AuthResponse DTO
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed for email {}: {}", registerRequest.getEmail(), e.getMessage());
            // Return 400 Bad Request for validation/business logic errors (e.g., email exists)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during registration for email {}: {}", registerRequest.getEmail(), e.getMessage(), e);
            // Return 500 Internal Server Error for unexpected issues
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred during registration.", e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Received login request for email: {}", loginRequest.getEmail());
        try {
            // Delegate authentication to the AuthService
            AuthResponse authResponse = authService.authenticateUser(loginRequest);
            log.info("User authenticated successfully: {}", loginRequest.getEmail());
            // Return the JWT token and user details in the response body (200 OK)
            return ResponseEntity.ok(authResponse);
        } catch (AuthenticationException e) { // Specific exception for authentication failures
            log.warn("Authentication failed for email {}: {}", loginRequest.getEmail(), e.getMessage());
            // Return 401 Unauthorized for invalid credentials
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Error: Invalid credentials!", e);
        } catch (Exception e) {
            log.error("Unexpected error during login for email {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            // Return 500 Internal Server Error for unexpected issues
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred during login.", e);
        }
    }
}