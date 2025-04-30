package com.registration.platform.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
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
@Table(name = "personal_info")
public class PersonalInfo {

    @Id
    private Long id; // Use the User's ID as the primary key for this entity

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId // Maps the 'id' field to the User's ID
    @JoinColumn(name = "user_id") // Name of the foreign key column
    private User user;

    @NotBlank(message = "Last name cannot be blank")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Last name must contain only letters, spaces, dots, apostrophes, or hyphens")
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    // Storing multiple first names as a single string, separated by space.
    // Frontend can handle the dynamic multi-field input.
    @NotBlank(message = "First name(s) cannot be blank")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "First name(s) must contain only letters, spaces, dots, apostrophes, or hyphens")
    @Size(max = 150)
    @Column(name = "first_names", nullable = false, length = 150)
    private String firstNames;

    @NotNull(message = "Gender cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @NotNull(message = "Date of birth cannot be null")
    @Past(message = "Date of birth must be in the past")
    // Age validation (>= 16) should ideally be handled by a custom validator or service logic
    // Adding a basic check here, but a dedicated validator is better.
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank(message = "Nationality cannot be blank")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nationality; // Store as string, frontend handles dynamic search

    @NotNull(message = "ID document type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "id_document_type", nullable = false, length = 30)
    private IdDocumentType idDocumentType;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Custom Logic/Validation Helpers (Example) ---

    // Basic age check - move to a dedicated validator for robustness
    @AssertTrue(message = "Applicant must be at least 16 years old")
    private boolean isAgeValid() {
        if (this.dateOfBirth == null) {
            return true; // Let @NotNull handle null case
        }
        return LocalDate.now().minusYears(16).isAfter(this.dateOfBirth) || LocalDate.now().minusYears(16).isEqual(this.dateOfBirth);
    }
}