package com.registration.platform.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.registration.platform.model.dto.ContactInfoDTO;
import com.registration.platform.service.ContactInfoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/applicant/contact-info") // Base path for contact info endpoints
@RequiredArgsConstructor
public class ContactInfoController {

    private static final Logger log = LoggerFactory.getLogger(ContactInfoController.class);
    private final ContactInfoService contactInfoService;

    @GetMapping
    public ResponseEntity<?> getContactInfo(Authentication authentication) {
        log.info("Received request to get contact info for user: {}", authentication.getName());
        try {
            return contactInfoService.getContactInfo(authentication)
                    .map(dto -> {
                        log.debug("Contact info found for user: {}", authentication.getName());
                        return ResponseEntity.ok(dto);
                    })
                    .orElseGet(() -> {
                        log.debug("No contact info found for user: {}", authentication.getName());
                        // Return 404 if no contact info has been saved yet
                        return ResponseEntity.notFound().build();
                    });
        } catch (UsernameNotFoundException e) {
            // This shouldn't happen if the user is authenticated, but handle defensively
            log.warn("Attempted to get contact info for non-existent authenticated user: {}", authentication.getName());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving contact info for user: {}", authentication.getName(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", e);
        }
    }

    @PutMapping // Use PUT for create/update semantics
    public ResponseEntity<?> saveOrUpdateContactInfo(@Valid @RequestBody ContactInfoDTO contactInfoDTO,
                                                     Authentication authentication) {
        log.info("Received request to save/update contact info for user: {}", authentication.getName());
        try {
            contactInfoService.saveOrUpdateContactInfo(contactInfoDTO, authentication);
            log.info("Successfully saved/updated contact info for user: {}", authentication.getName());

            // Re-fetch the DTO to include the potentially updated emailVerified status (though this endpoint doesn't change it)
            // and ensure consistency.
            return contactInfoService.getContactInfo(authentication)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve updated contact info after save.")); // Should not happen

        } catch (UsernameNotFoundException e) {
             log.warn("Update contact info failed for non-existent authenticated user: {}", authentication.getName());
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) { // Catch potential validation errors from service layer if any
            log.warn("Validation error during contact info update for user {}: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error saving/updating contact info for user: {}", authentication.getName(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", e);
        }
    }

    // Endpoints for email verification (e.g., POST /verify-email, GET /confirm-email?token=...)
    // would be added here or in AuthController later.
}