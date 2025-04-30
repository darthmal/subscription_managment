package com.registration.platform.model.entity;

public enum DocumentStatus {
    MISSING,             // Document has not been uploaded yet
    UPLOADED,            // Successfully uploaded, awaiting validation
    VALIDATION_PENDING,  // In the queue for manual/automated validation
    VALIDATION_FAILED,   // Automated validation failed (e.g., format, OCR)
    VALIDATED,           // Successfully validated
    REJECTED             // Manually rejected by an admin
}