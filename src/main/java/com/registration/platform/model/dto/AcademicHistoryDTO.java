package com.registration.platform.model.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicHistoryDTO {

    // ID is included for identifying existing records for update/delete,
    // and will be present in responses. It should be null for new entries.
    private Long id;

    @NotBlank(message = "Institution name cannot be blank")
    @Size(max = 255)
    private String institutionName;

    @NotBlank(message = "Specialization cannot be blank")
    @Size(max = 255)
    private String specialization;

    @NotNull(message = "Start date cannot be null")
    @PastOrPresent(message = "Start date must be in the past or present")
    private LocalDate startDate;

    // End date can be null if ongoing
    @PastOrPresent(message = "End date must be in the past or present")
    private LocalDate endDate;

    // Basic validation: endDate must be after startDate if present.
    // More complex overlap validation will be in the service.
    // @AssertTrue(message = "End date must be after start date")
    // private boolean isEndDateValid() {
    //     return endDate == null || startDate == null || !endDate.isBefore(startDate);
    // }
    // Note: Bean Validation on DTOs for cross-field validation can be tricky.
    // It's often more robust to handle this in the service layer.
}