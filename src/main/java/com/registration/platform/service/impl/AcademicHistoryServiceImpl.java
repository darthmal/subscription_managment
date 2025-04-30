package com.registration.platform.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.registration.platform.exception.ResourceNotFoundException;
import com.registration.platform.model.dto.AcademicHistoryDTO;
import com.registration.platform.model.entity.AcademicHistory;
import com.registration.platform.model.entity.User;
import com.registration.platform.repository.AcademicHistoryRepository;
import com.registration.platform.repository.UserRepository;
import com.registration.platform.service.AcademicHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AcademicHistoryServiceImpl implements AcademicHistoryService {

    private static final Logger log = LoggerFactory.getLogger(AcademicHistoryServiceImpl.class);

    private final AcademicHistoryRepository academicHistoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AcademicHistoryDTO> getAcademicHistory(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        log.debug("Fetching academic history for user ID: {}", currentUser.getId());
        // Rely on the @OrderBy in the User entity for ordering, or use specific repo method
        // List<AcademicHistory> historyList = academicHistoryRepository.findByUserOrderByStartDateDesc(currentUser);
        List<AcademicHistory> historyList = currentUser.getAcademicHistories(); // Assumes eager/initialized fetch or within transaction
        return historyList.stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AcademicHistoryDTO addAcademicHistory(AcademicHistoryDTO historyDTO, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        log.info("Adding academic history for user ID: {}", currentUser.getId());

        if (historyDTO.getId() != null) {
            throw new IllegalArgumentException("ID must be null when adding new academic history.");
        }

        // Validate dates (end >= start) and check for overlaps
        validateAcademicDates(historyDTO.getStartDate(), historyDTO.getEndDate(), null, currentUser);

        AcademicHistory newHistory = mapDtoToEntity(historyDTO);
        // Set the relationship using the helper method in User entity
        currentUser.addAcademicHistory(newHistory);

        // Saving the user cascades the save to the new AcademicHistory
        userRepository.save(currentUser);
        log.info("Successfully added academic history with ID: {} for user ID: {}", newHistory.getId(), currentUser.getId());

        // Find the saved entity to get the generated ID for the DTO
        // This assumes the list is updated correctly after save
        AcademicHistory savedHistory = currentUser.getAcademicHistories().stream()
                                           .filter(h -> h.getInstitutionName().equals(historyDTO.getInstitutionName()) && h.getStartDate().equals(historyDTO.getStartDate()))
                                           .findFirst()
                                           .orElseThrow(() -> new IllegalStateException("Could not find saved academic history")); // Should not happen

        return mapEntityToDto(savedHistory);
    }

    @Override
    @Transactional
    public AcademicHistoryDTO updateAcademicHistory(Long historyId, AcademicHistoryDTO historyDTO, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        log.info("Updating academic history ID: {} for user ID: {}", historyId, currentUser.getId());

        AcademicHistory existingHistory = findHistoryByIdAndUser(historyId, currentUser);

        // Validate dates (end >= start) and check for overlaps (excluding the current record)
        validateAcademicDates(historyDTO.getStartDate(), historyDTO.getEndDate(), historyId, currentUser);

        // Update fields from DTO
        existingHistory.setInstitutionName(historyDTO.getInstitutionName());
        existingHistory.setSpecialization(historyDTO.getSpecialization());
        existingHistory.setStartDate(historyDTO.getStartDate());
        existingHistory.setEndDate(historyDTO.getEndDate());
        // Timestamps updated by @PreUpdate

        AcademicHistory updatedHistory = academicHistoryRepository.save(existingHistory);
        log.info("Successfully updated academic history ID: {}", updatedHistory.getId());

        return mapEntityToDto(updatedHistory);
    }

    @Override
    @Transactional
    public void deleteAcademicHistory(Long historyId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        log.info("Deleting academic history ID: {} for user ID: {}", historyId, currentUser.getId());

        AcademicHistory historyToDelete = findHistoryByIdAndUser(historyId, currentUser);

        // Remove using the helper method in User entity to manage relationship and trigger orphanRemoval
        currentUser.removeAcademicHistory(historyToDelete);
        userRepository.save(currentUser); // Persist changes to the User's collection

        // Alternatively, delete directly via repository if cascade/orphanRemoval handles it:
        // academicHistoryRepository.delete(historyToDelete);

        log.info("Successfully deleted academic history ID: {}", historyId);
    }

    // --- Helper Methods ---

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User must be authenticated to perform this operation.");
        }
        String email = authentication.getName();
        // Consider fetching user with academic history eagerly if needed outside transaction
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    private AcademicHistory findHistoryByIdAndUser(Long historyId, User user) {
        return academicHistoryRepository.findById(historyId)
                .filter(history -> Objects.equals(history.getUser().getId(), user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("AcademicHistory", "id", historyId));
    }

    private void validateAcademicDates(LocalDate startDate, LocalDate endDate, Long currentHistoryId, User user) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null.");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        // Overlap Check
        List<AcademicHistory> otherHistories = user.getAcademicHistories().stream()
                .filter(h -> !Objects.equals(h.getId(), currentHistoryId)) // Exclude the record being updated (if any)
                .toList();

        for (AcademicHistory existing : otherHistories) {
            if (periodsOverlap(startDate, endDate, existing.getStartDate(), existing.getEndDate())) {
                log.warn("Date overlap detected for user ID {}. New/Updated: [{}, {}], Existing ID {}: [{}, {}]",
                         user.getId(), startDate, endDate, existing.getId(), existing.getStartDate(), existing.getEndDate());
                throw new IllegalArgumentException("Academic history periods cannot overlap.");
            }
        }
        log.debug("Date validation and overlap check passed for user ID {}, period [{}, {}]", user.getId(), startDate, endDate);
    }

    private boolean periodsOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        // Treat null end dates as ongoing (effectively infinity)
        LocalDate effectiveEnd1 = (end1 == null) ? LocalDate.MAX : end1;
        LocalDate effectiveEnd2 = (end2 == null) ? LocalDate.MAX : end2;

        // Overlap occurs if start1 <= effectiveEnd2 AND effectiveEnd1 >= start2
        return !start1.isAfter(effectiveEnd2) && !effectiveEnd1.isBefore(start2);
    }


    // Manual mapping (Consider MapStruct)
    private AcademicHistoryDTO mapEntityToDto(AcademicHistory entity) {
        return AcademicHistoryDTO.builder()
                .id(entity.getId())
                .institutionName(entity.getInstitutionName())
                .specialization(entity.getSpecialization())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .build();
    }

    private AcademicHistory mapDtoToEntity(AcademicHistoryDTO dto) {
        // Note: We don't set the User here; it's handled by the addAcademicHistory helper method.
        return AcademicHistory.builder()
                // ID is not mapped from DTO for new entities
                .institutionName(dto.getInstitutionName())
                .specialization(dto.getSpecialization())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
    }
}