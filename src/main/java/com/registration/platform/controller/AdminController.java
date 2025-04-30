package com.registration.platform.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping; // Import RequestBody, PutMapping etc.
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping; // Import exception
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping; // Import DocumentDTO
import org.springframework.web.bind.annotation.RestController; // Import Status DTO
import org.springframework.web.server.ResponseStatusException;

import com.registration.platform.exception.ResourceNotFoundException;
import com.registration.platform.model.dto.ApplicationDetailDTO; // Import Valid
import com.registration.platform.model.dto.DocumentDTO;
import com.registration.platform.model.dto.DocumentStatusUpdateDTO;
import com.registration.platform.model.dto.UserSummaryDTO;
import com.registration.platform.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Note: Access to this controller is already restricted to ROLE_ADMIN
// by the SecurityConfig rule for "/api/admin/**".
// @PreAuthorize("hasRole('ADMIN')") // Could be added for extra method-level security if needed

@RestController
@RequestMapping("/api/admin/users") // Base path for user management by admin
@RequiredArgsConstructor
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<Page<UserSummaryDTO>> getAllUsers(Pageable pageable) {
        // Pageable object is automatically populated by Spring from request parameters
        // (e.g., ?page=0&size=20&sort=email,asc)
        log.info("Admin request received to list users with pagination: {}", pageable);
        try {
            Page<UserSummaryDTO> userPage = adminService.getAllUsers(pageable);
            return ResponseEntity.ok(userPage);
        } catch (Exception e) {
            log.error("Error retrieving users for admin: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve users", e);
        }
    }

    @GetMapping("/{userId}/application")
    public ResponseEntity<ApplicationDetailDTO> getApplicationDetails(@PathVariable Long userId) {
        log.info("Admin request received for application details of user ID: {}", userId);
        try {
            return adminService.getApplicationDetails(userId)
                    .map(ResponseEntity::ok) // If found, return 200 OK with the DTO
                    .orElseThrow(() -> { // If not found, throw 404
                        log.warn("Application details not found for user ID: {}", userId);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Application details not found for user ID: " + userId);
                    });
        } catch (ResponseStatusException rse) {
            throw rse; // Re-throw specific exceptions
        } catch (Exception e) {
            log.error("Error retrieving application details for user ID {}: {}", userId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve application details", e);
        }
    }

    // Endpoint to update document status (could be in a separate DocumentAdminController)
    @PutMapping("/documents/{documentId}/status")
    public ResponseEntity<?> updateDocumentStatus(@PathVariable Long documentId,
                                                  @Valid @RequestBody DocumentStatusUpdateDTO statusUpdateDTO) {
        log.info("Admin request received to update status for document ID: {} to {}", documentId, statusUpdateDTO.getNewStatus());
        try {
            DocumentDTO updatedDocument = adminService.updateDocumentStatus(documentId, statusUpdateDTO);
            log.info("Document ID: {} status updated successfully to {}", documentId, updatedDocument.getStatus());
            return ResponseEntity.ok(updatedDocument);
        } catch (ResourceNotFoundException e) {
            log.warn("Update status failed, document ID {} not found: {}", documentId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.warn("Update status failed for document ID {}: {}", documentId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage()); // Return 400 for invalid transitions/data
        } catch (Exception e) {
            log.error("Error updating status for document ID {}: {}", documentId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update document status", e);
        }
    }


    // Add other admin endpoints here later:
    // - GET /api/admin/applications -> List applications (might be different from users)
    // - GET /api/admin/dashboard/stats
    // - GET /api/admin/applications/export?format=csv
}