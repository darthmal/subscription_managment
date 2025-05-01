package com.registration.platform.model.entity;

public enum ApplicationStatus {
    PENDING,  // Initial status after registration
    APPROVED, // Application has been approved
    REJECTED,  // Application has been rejected
    ACTIVE // Only for admin
    // We might add other statuses later, e.g., IN_REVIEW, NEEDS_MORE_INFO
}