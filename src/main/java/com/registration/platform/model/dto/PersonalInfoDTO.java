package com.registration.platform.model.dto;

import java.time.LocalDate;

import com.registration.platform.model.entity.Gender;
import com.registration.platform.model.entity.IdDocumentType;

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
public class PersonalInfoDTO {

    // We don't include userId here, as it will be derived from the authenticated user context

    @NotBlank(message = "Last name cannot be blank")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Last name must contain only letters, spaces, dots, apostrophes, or hyphens")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "First name(s) cannot be blank")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "First name(s) must contain only letters, spaces, dots, apostrophes, or hyphens")
    @Size(max = 150)
    private String firstNames;

    @NotNull(message = "Gender cannot be null")
    private Gender gender;

    @NotNull(message = "Date of birth cannot be null")
    @Past(message = "Date of birth must be in the past")
    // Age validation (>= 16) will be handled in the service layer
    private LocalDate dateOfBirth;

    @NotBlank(message = "Nationality cannot be blank")
    @Size(max = 100)
    private String nationality;

    @NotNull(message = "ID document type cannot be null")
    private IdDocumentType idDocumentType;

    // Timestamps (createdAt, updatedAt) are usually handled by the backend and not included in request DTOs
    // They might be included in response DTOs if needed by the frontend.
}