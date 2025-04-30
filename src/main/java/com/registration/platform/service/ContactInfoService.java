package com.registration.platform.service;

import java.util.Optional;

import org.springframework.security.core.Authentication; // Import entity

import com.registration.platform.model.dto.ContactInfoDTO; // Import Authentication
import com.registration.platform.model.entity.ContactInfo;

public interface ContactInfoService {

    /**
     * Retrieves the contact information for the currently authenticated user.
     * Includes email verification status from the related ContactInfo entity.
     *
     * @param authentication The current user's authentication object.
     * @return An Optional containing the ContactInfoDTO if found, otherwise empty.
     */
    Optional<ContactInfoDTO> getContactInfo(Authentication authentication);

    /**
     * Saves or updates the contact information for the currently authenticated user.
     * Handles phone number, address, and emergency contact details.
     * Does NOT handle email verification status update (that requires a separate flow).
     *
     * @param contactInfoDTO The DTO containing the contact information to save/update.
     * @param authentication The current user's authentication object.
     * @return The saved/updated ContactInfo entity.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if the user cannot be found.
     */
    ContactInfo saveOrUpdateContactInfo(ContactInfoDTO contactInfoDTO, Authentication authentication);

    // Methods related to email verification (e.g., initiateVerification, confirmVerification)
    // would likely go here or in a dedicated EmailVerificationService later.

}