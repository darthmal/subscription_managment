package com.registration.platform.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    // Add user details needed on the frontend after login
    private Long userId;
    private String email;
    private java.util.Set<String> roles; // Use Set for roles

}