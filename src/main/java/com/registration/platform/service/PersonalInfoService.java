package com.registration.platform.service;

import java.util.Optional;

import org.springframework.security.core.Authentication; // Import entity

import com.registration.platform.model.dto.PersonalInfoDTO; // Import Authentication
import com.registration.platform.model.entity.PersonalInfo;

public interface PersonalInfoService {

    /**
     * Retrieves the personal information for the currently authenticated user.
     *
     * @param authentication The current user's authentication object.
     * @return An Optional containing the PersonalInfoDTO if found, otherwise empty.
     */
    Optional<PersonalInfoDTO> getPersonalInfo(Authentication authentication);

    /**
     * Saves or updates the personal information for the currently authenticated user.
     * Performs validation, including age check.
     *
     * @param personalInfoDTO The DTO containing the personal information to save/update.
     * @param authentication  The current user's authentication object.
     * @return The saved/updated PersonalInfo entity.
     * @throws IllegalArgumentException if validation fails (e.g., age requirement not met).
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if the user cannot be found.
     */
    PersonalInfo saveOrUpdatePersonalInfo(PersonalInfoDTO personalInfoDTO, Authentication authentication);

}