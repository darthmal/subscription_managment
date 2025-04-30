package com.registration.platform.service.impl;

import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.registration.platform.model.dto.PersonalInfoDTO;
import com.registration.platform.model.entity.PersonalInfo;
import com.registration.platform.model.entity.User;
import com.registration.platform.repository.UserRepository;
import com.registration.platform.service.PersonalInfoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PersonalInfoServiceImpl implements PersonalInfoService {

    private static final Logger log = LoggerFactory.getLogger(PersonalInfoServiceImpl.class);
    private static final int MIN_AGE = 16; // Minimum required age

    private final UserRepository userRepository;
    // PersonalInfoRepository might not be strictly needed if we always cascade from User,
    // but can be useful for direct lookups if required later.
    // private final PersonalInfoRepository personalInfoRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonalInfoDTO> getPersonalInfo(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        return Optional.ofNullable(currentUser.getPersonalInfo())
                .map(this::mapEntityToDto); // Map entity to DTO if present
    }

    @Override
    @Transactional
    public PersonalInfo saveOrUpdatePersonalInfo(PersonalInfoDTO personalInfoDTO, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        log.info("Saving/updating personal info for user ID: {}", currentUser.getId());

        // Validate age
        validateAge(personalInfoDTO.getDateOfBirth());

        PersonalInfo personalInfo = currentUser.getPersonalInfo();

        if (personalInfo == null) {
            log.debug("No existing personal info found for user ID: {}. Creating new.", currentUser.getId());
            personalInfo = new PersonalInfo();
            // No need to set ID or User here, @MapsId and cascade handle it
        } else {
            log.debug("Existing personal info found for user ID: {}. Updating.", currentUser.getId());
        }

        // Map DTO fields to entity
        mapDtoToEntity(personalInfoDTO, personalInfo);

        // Set the relationship on the User side to ensure consistency and trigger cascade
        currentUser.setPersonalInfo(personalInfo);

        // Save the User entity, which will cascade the save/update to PersonalInfo
        userRepository.save(currentUser);
        log.info("Successfully saved/updated personal info for user ID: {}", currentUser.getId());

        // Return the managed PersonalInfo entity associated with the user
        return currentUser.getPersonalInfo();
    }

    // --- Helper Methods ---

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User must be authenticated to perform this operation.");
        }
        String email = authentication.getName(); // Assuming email is the principal name
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    private void validateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be null."); // Should be caught by @NotNull, but double-check
        }
        if (LocalDate.now().minusYears(MIN_AGE).isBefore(dateOfBirth)) {
            log.warn("Age validation failed for date of birth: {}", dateOfBirth);
            throw new IllegalArgumentException("Applicant must be at least " + MIN_AGE + " years old.");
        }
        log.debug("Age validation passed for date of birth: {}", dateOfBirth);
    }

    // Manual mapping methods (Consider using MapStruct for more complex scenarios)
    private PersonalInfoDTO mapEntityToDto(PersonalInfo entity) {
        return PersonalInfoDTO.builder()
                .lastName(entity.getLastName())
                .firstNames(entity.getFirstNames())
                .gender(entity.getGender())
                .dateOfBirth(entity.getDateOfBirth())
                .nationality(entity.getNationality())
                .idDocumentType(entity.getIdDocumentType())
                .build();
    }

    private void mapDtoToEntity(PersonalInfoDTO dto, PersonalInfo entity) {
        entity.setLastName(dto.getLastName());
        entity.setFirstNames(dto.getFirstNames());
        entity.setGender(dto.getGender());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setNationality(dto.getNationality());
        entity.setIdDocumentType(dto.getIdDocumentType());
        // Timestamps are handled by @PrePersist/@PreUpdate
    }
}