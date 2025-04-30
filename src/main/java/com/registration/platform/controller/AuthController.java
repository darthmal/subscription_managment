package com.registration.platform.controller;

import org.springframework.http.HttpStatus; // Import AuthResponse
import org.springframework.http.ResponseEntity; // Import LoginRequest
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    private final AuthService authService; // Inject the AuthService

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // Delegate the registration logic to the AuthService
        // The service will handle validation, password encoding, saving the user, etc.
        try {
            authService.registerUser(registerRequest);
            // Consider returning a more specific success message or the created user details (without password)
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
        } catch (Exception e) {
            // Catch potential exceptions (like email already exists) from the service
            // A more robust error handling mechanism (e.g., @ControllerAdvice) is recommended for production
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Delegate authentication to the AuthService
            AuthResponse authResponse = authService.authenticateUser(loginRequest);
            // Return the JWT token in the response body
            return ResponseEntity.ok(authResponse);
        } catch (AuthenticationException e) {
            // Handle failed authentication attempts (e.g., bad credentials)
            // A more specific exception handler (@ControllerAdvice) is better for production
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Invalid credentials!");
        } catch (Exception e) {
            // Catch other potential errors during login
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}