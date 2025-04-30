package com.registration.platform.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "contact_info")
public class ContactInfo {

    @Id
    private Long id; // Use the User's ID as the primary key

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId // Maps the 'id' field to the User's ID
    @JoinColumn(name = "user_id")
    private User user;

    // Email is managed in the User entity, but we track verification status here
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    // We might add fields for verification tokens/timestamps if implementing double opt-in
    // private String emailVerificationToken;
    // private LocalDateTime emailVerificationTokenExpiry;

    @NotBlank(message = "Phone number cannot be blank")
    @Size(max = 30)
    // Basic pattern - consider libphonenumber for real validation/formatting
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]+$", message = "Invalid phone number format")
    @Column(name = "phone_number", nullable = false, length = 30)
    private String phoneNumber;

    // Embed the Address details
    @Valid // Enable validation of embedded fields
    @Embedded
    @NotNull(message = "Address cannot be null") // Ensure the embedded object itself is not null
    private Address address;

    // Embed the Emergency Contact details
    @Valid // Enable validation of embedded fields
    @Embedded
    @NotNull(message = "Emergency contact cannot be null") // Ensure the embedded object itself is not null
    private EmergencyContact emergencyContact;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Ensure embedded objects are initialized if built partially
        if (address == null) address = new Address();
        if (emergencyContact == null) emergencyContact = new EmergencyContact();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}