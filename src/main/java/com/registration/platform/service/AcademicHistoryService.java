package com.registration.platform.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.registration.platform.model.dto.AcademicHistoryDTO;

public interface AcademicHistoryService {

    /**
     * Retrieves all academic history entries for the currently authenticated user.
     *
     * @param authentication The current user's authentication object.
     * @return A list of AcademicHistoryDTOs, ordered by start date descending.
     */
    List<AcademicHistoryDTO> getAcademicHistory(Authentication authentication);

    /**
     * Adds a new academic history entry for the currently authenticated user.
     * Performs validation, including date overlap checks.
     *
     * @param historyDTO     The DTO containing the new academic history details (ID should be null).
     * @param authentication The current user's authentication object.
     * @return The created AcademicHistoryDTO with its generated ID.
     * @throws IllegalArgumentException if validation fails (e.g., date overlap, end date before start date).
     */
    AcademicHistoryDTO addAcademicHistory(AcademicHistoryDTO historyDTO, Authentication authentication);

    /**
     * Updates an existing academic history entry for the currently authenticated user.
     * Performs validation, including date overlap checks against other entries.
     *
     * @param historyId      The ID of the academic history entry to update.
     * @param historyDTO     The DTO containing the updated academic history details.
     * @param authentication The current user's authentication object.
     * @return The updated AcademicHistoryDTO.
     * @throws IllegalArgumentException if validation fails or the entry doesn't belong to the user.
     * @throws com.registration.platform.exception.ResourceNotFoundException if the historyId is not found.
     */
    AcademicHistoryDTO updateAcademicHistory(Long historyId, AcademicHistoryDTO historyDTO, Authentication authentication);

    /**
     * Deletes an academic history entry for the currently authenticated user.
     *
     * @param historyId      The ID of the academic history entry to delete.
     * @param authentication The current user's authentication object.
     * @throws IllegalArgumentException if the entry doesn't belong to the user.
     * @throws com.registration.platform.exception.ResourceNotFoundException if the historyId is not found.
     */
    void deleteAcademicHistory(Long historyId, Authentication authentication);

}