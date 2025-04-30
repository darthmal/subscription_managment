package com.registration.platform.model.entity;

import lombok.Getter;

@Getter // Useful for accessing properties like requiresValidation
public enum DocumentType {
    // Diplomas
    DIPLOMA_BAC("Baccalaureate / High School Diploma", true, new String[]{"application/pdf"}, 5 * 1024 * 1024), // 5MB
    DIPLOMA_HIGHER("Higher Education Diploma", true, new String[]{"application/pdf"}, 5 * 1024 * 1024), // 5MB

    // ID Documents
    ID_CARD_FRONT("National ID Card (Front)", true, new String[]{"image/jpeg", "image/png"}, 2 * 1024 * 1024), // 2MB Example
    ID_CARD_BACK("National ID Card (Back)", true, new String[]{"image/jpeg", "image/png"}, 2 * 1024 * 1024), // 2MB Example
    BIRTH_CERTIFICATE("Birth Certificate", true, new String[]{"application/pdf"}, 3 * 1024 * 1024), // 3MB Example

    // Photo
    ID_PHOTO("ID Photo", true, new String[]{"image/jpeg", "image/png"}, 1 * 1024 * 1024); // 1MB Example

    private final String description;
    private final boolean requiresValidation; // Indicates if manual/automated validation is needed
    private final String[] allowedContentTypes;
    private final long maxSizeInBytes; // Max size allowed for this specific document type

    DocumentType(String description, boolean requiresValidation, String[] allowedContentTypes, long maxSizeInBytes) {
        this.description = description;
        this.requiresValidation = requiresValidation;
        this.allowedContentTypes = allowedContentTypes;
        this.maxSizeInBytes = maxSizeInBytes;
    }

    // Helper method to check content type
    public boolean isContentTypeAllowed(String contentType) {
        if (contentType == null) return false;
        for (String allowedType : allowedContentTypes) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
}