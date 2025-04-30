package com.registration.platform.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime; // Import AssertTrue

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne; // Using LocalDate for simplicity, YearMonth could also be used
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
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
@Entity
@Table(name = "academic_history", indexes = {
        @Index(name = "idx_academic_history_user", columnList = "user_id")
})
public class AcademicHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The applicant this history belongs to

    @NotBlank(message = "Institution name cannot be blank")
    @Size(max = 255)
    @Column(name = "institution_name", nullable = false)
    private String institutionName; // Name obtained from autocomplete API

    @NotBlank(message = "Specialization cannot be blank")
    @Size(max = 255)
    @Column(nullable = false)
    private String specialization; // Selected specialization/major

    @NotNull(message = "Start date cannot be null")
    @PastOrPresent(message = "Start date must be in the past or present")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // End date can be null if the study period is ongoing
    @PastOrPresent(message = "End date must be in the past or present")
    @Column(name = "end_date")
    private LocalDate endDate;

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

    // Custom validation for date overlap will be handled in the service layer,
    // as it requires checking against other AcademicHistory entries for the same user.
    // Basic validation: endDate must be after startDate if present.
    @AssertTrue(message = "End date must be after start date")
    private boolean isEndDateAfterStartDate() {
        if (this.endDate == null || this.startDate == null) {
            return true; // Allow null end date or if start date is null (should be caught by @NotNull)
        }
        return this.endDate.isAfter(this.startDate) || this.endDate.isEqual(this.startDate);
    }
}