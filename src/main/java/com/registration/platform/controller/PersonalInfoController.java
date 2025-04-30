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

import com.registration.platform.model.dto.PersonalInfoDTO;
import com.registration.platform.service.PersonalInfoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/applicant/personal-info") // Base path for personal info endpoints
@RequiredArgsConstructor
public class PersonalInfoController {

    private static final Logger log = LoggerFactory.getLogger(PersonalInfoController.class);
    private final PersonalInfoService personalInfoService;

    @GetMapping
    public ResponseEntity<?> getPersonalInfo(Authentication authentication) {
        log.info("Received request to get personal info for user: {}", authentication.getName());
        try {
            return personalInfoService.getPersonalInfo(authentication)
                    .map(dto -> {
                        log.debug("Personal info found for user: {}", authentication.getName());
                        return ResponseEntity.ok(dto);
                    })
                    .orElseGet(() -> {
                        log.debug("No personal info found for user: {}", authentication.getName());
                        return ResponseEntity.notFound().build();
                    });
        } catch (UsernameNotFoundException e) {
            log.warn("Attempted to get personal info for non-existent user: {}", authentication.getName());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving personal info for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PutMapping // Use PUT for create/update semantics
    public ResponseEntity<?> saveOrUpdatePersonalInfo(@Valid @RequestBody PersonalInfoDTO personalInfoDTO,
                                                      Authentication authentication) {
        log.info("Received request to save/update personal info for user: {}", authentication.getName());
        try {
            // Save or update the personal info
            personalInfoService.saveOrUpdatePersonalInfo(personalInfoDTO, authentication);
            log.info("Successfully saved/updated personal info for user: {}", authentication.getName());

            // Return 200 OK with the DTO that was processed
            return ResponseEntity.ok(personalInfoDTO);

            // Alternative: Return 200 OK with no body
            // return ResponseEntity.ok().build();

            // Alternative: Return 200 OK with a simple success message
            // return ResponseEntity.ok("Personal information updated successfully.");

        } catch (IllegalArgumentException | UsernameNotFoundException e) {
            log.warn("Validation or user not found error during personal info update for user {}: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error saving/updating personal info for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }
}