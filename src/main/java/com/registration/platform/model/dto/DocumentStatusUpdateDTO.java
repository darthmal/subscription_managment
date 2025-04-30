package com.registration.platform.model.dto;

import com.registration.platform.model.entity.DocumentStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatusUpdateDTO {

    @NotNull(message = "New status cannot be null")
    private DocumentStatus newStatus;

    @Size(max = 500, message = "Validation notes cannot exceed 500 characters")
    private String validationNotes; // Optional notes
}