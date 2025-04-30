package com.registration.platform.service;

import com.registration.platform.model.dto.AuthResponse;
import com.registration.platform.model.dto.LoginRequest;
import com.registration.platform.model.dto.RegisterRequest;

public interface AuthService {

    /**
     * Registers a new user based on the provided registration request.
     *
     * @param registerRequest DTO containing user registration details.
     * @return AuthResponse DTO containing the JWT access token and user details.
     * @throws IllegalArgumentException if the email address is already taken.
     */
    AuthResponse registerUser(RegisterRequest registerRequest);

    /**
     * Authenticates a user based on login credentials and returns a JWT token.
     *
     * @param loginRequest DTO containing user login details (email, password).
     * @return AuthResponse DTO containing the JWT access token.
     * @throws org.springframework.security.core.AuthenticationException if authentication fails.
     */
    AuthResponse authenticateUser(LoginRequest loginRequest);
}