package com.registration.platform.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.registration.platform.exception.ResourceNotFoundException;
import com.registration.platform.model.dto.AcademicHistoryDTO;
import com.registration.platform.service.AcademicHistoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/applicant/academic-history") // Base path for academic history
@RequiredArgsConstructor
public class AcademicHistoryController {

    private static final Logger log = LoggerFactory.getLogger(AcademicHistoryController.class);
    private final AcademicHistoryService academicHistoryService;

    @GetMapping
    public ResponseEntity<List<AcademicHistoryDTO>> getAcademicHistory(Authentication authentication) {
        log.debug("Received request to get academic history for user: {}", authentication.getName());
        try {
            List<AcademicHistoryDTO> historyList = academicHistoryService.getAcademicHistory(authentication);
            return ResponseEntity.ok(historyList);
        } catch (Exception e) {
            log.error("Error retrieving academic history for user {}: {}", authentication.getName(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve academic history", e);
        }
    }

    @PostMapping
    public ResponseEntity<?> addAcademicHistory(@Valid @RequestBody AcademicHistoryDTO historyDTO,
                                                Authentication authentication) {
        log.info("Received request to add academic history for user: {}", authentication.getName());
        try {
            AcademicHistoryDTO createdDto = academicHistoryService.addAcademicHistory(historyDTO, authentication);
            log.info("Academic history added successfully for user: {}, ID: {}", authentication.getName(), createdDto.getId());
            // Return 201 Created with the created DTO (including its new ID)
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
        } catch (IllegalArgumentException e) {
            log.warn("Add academic history failed for user {}: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error adding academic history for user {}: {}", authentication.getName(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add academic history", e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAcademicHistory(@PathVariable Long id,
                                                   @Valid @RequestBody AcademicHistoryDTO historyDTO,
                                                   Authentication authentication) {
        log.info("Received request to update academic history ID: {} for user: {}", id, authentication.getName());
        try {
            AcademicHistoryDTO updatedDto = academicHistoryService.updateAcademicHistory(id, historyDTO, authentication);
            log.info("Academic history ID: {} updated successfully for user: {}", id, authentication.getName());
            return ResponseEntity.ok(updatedDto);
        } catch (ResourceNotFoundException e) {
            log.warn("Update failed, academic history ID {} not found for user {}: {}", id, authentication.getName(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.warn("Update academic history ID {} failed for user {}: {}", id, authentication.getName(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating academic history ID {} for user {}: {}", id, authentication.getName(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update academic history", e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAcademicHistory(@PathVariable Long id,
                                                   Authentication authentication) {
        log.info("Received request to delete academic history ID: {} for user: {}", id, authentication.getName());
        try {
            academicHistoryService.deleteAcademicHistory(id, authentication);
            log.info("Academic history ID: {} deleted successfully for user: {}", id, authentication.getName());
            return ResponseEntity.ok("Academic history entry deleted successfully."); // Or ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.warn("Delete failed, academic history ID {} not found for user {}: {}", id, authentication.getName(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
             // This might occur if the service adds extra checks, though ResourceNotFound usually covers ownership
            log.warn("Delete academic history ID {} failed for user {}: {}", id, authentication.getName(), e.getMessage());
             return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting academic history ID {} for user {}: {}", id, authentication.getName(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete academic history", e);
        }
    }
}