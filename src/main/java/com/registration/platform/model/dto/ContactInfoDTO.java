package com.registration.platform.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfoDTO {

    // Email is read from the User entity, not set via this DTO
    // private String email;

    // Email verification status is usually read-only for the user via API
    private boolean emailVerified;

    @NotBlank(message = "Phone number cannot be blank")
    @Size(max = 30)
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]+$", message = "Invalid phone number format")
    private String phoneNumber;

    @Valid // Enable validation of nested AddressDTO
    @NotNull(message = "Address cannot be null")
    private AddressDTO address;

    @Valid // Enable validation of nested EmergencyContactDTO
    @NotNull(message = "Emergency contact cannot be null")
    private EmergencyContactDTO emergencyContact;

    // Timestamps usually excluded from request DTOs
}