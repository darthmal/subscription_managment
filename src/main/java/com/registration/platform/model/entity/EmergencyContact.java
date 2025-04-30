package com.registration.platform.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
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
@Embeddable
public class EmergencyContact {

    @NotBlank(message = "Emergency contact name cannot be blank")
    @Size(max = 150)
    @Column(name = "emergency_contact_name")
    private String name;

    @NotBlank(message = "Emergency contact relationship cannot be blank")
    @Size(max = 100)
    @Column(name = "emergency_contact_relationship")
    private String relationship;

    @NotBlank(message = "Emergency contact phone number cannot be blank")
    @Size(max = 30)
    // Basic pattern for digits, plus, spaces, hyphens - consider libphonenumber for real validation
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]+$", message = "Invalid phone number format")
    @Column(name = "emergency_contact_phone")
    private String phone;
}