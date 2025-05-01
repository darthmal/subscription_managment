package com.registration.platform.service.impl;

import java.time.LocalDateTime; // Import exception
import java.util.List;
import java.util.Map;
import java.util.Optional; // Import Status DTO
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // Import DocumentStatus enum
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // Import EmailService

import com.registration.platform.exception.ResourceNotFoundException;
import com.registration.platform.model.dto.ApplicationDetailDTO;
import com.registration.platform.model.dto.DocumentDTO;
import com.registration.platform.model.dto.DocumentStatusUpdateDTO;
import com.registration.platform.model.dto.UserSummaryDTO; // Import all entities
import com.registration.platform.model.entity.ApplicationStatus;
import com.registration.platform.model.entity.Document;
import com.registration.platform.model.entity.DocumentStatus;
import com.registration.platform.model.entity.Role;
import com.registration.platform.model.entity.User; // Import LocalDateTime
import com.registration.platform.repository.DocumentRepository;
import com.registration.platform.repository.UserRepository;
import com.registration.platform.service.AdminService;
import com.registration.platform.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final EmailService emailService; // Inject EmailService for notifications

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryDTO> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination: {}", pageable);
        Page<User> userPage = userRepository.findAll(pageable);
        // Convert the Page<User> to Page<UserSummaryDTO>
        return userPage.map(UserSummaryDTO::fromUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDTO> getAllUsersList() {
        log.warn("Fetching all users as a list. Consider using pagination for large datasets.");
        List<User> users = userRepository.findAll(); // Potentially inefficient for large numbers
        return users.stream()
                .map(UserSummaryDTO::fromUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApplicationDetailDTO> getApplicationDetails(Long userId) {
        log.debug("Fetching application details for user ID: {}", userId);

        // Fetch the user by ID. Use Optional for handling not found cases.
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            log.warn("User not found with ID: {}", userId);
            return Optional.empty(); // Return empty Optional if user doesn't exist
        }

        User user = userOptional.get();

        // Fetch associated documents for this user
        List<Document> documents = documentRepository.findByUserOrderByUploadedAtDesc(user);
        List<DocumentDTO> documentDTOs = documents.stream()
                .map(this::mapDocumentEntityToDto) // Use a local mapping method
                .collect(Collectors.toList());

        // Use the factory method in ApplicationDetailDTO to map basic user info + related entities
        ApplicationDetailDTO applicationDetailDTO = ApplicationDetailDTO.fromUser(user);

        // Set the fetched documents into the DTO
        applicationDetailDTO.setDocuments(documentDTOs);

        log.info("Successfully fetched application details for user ID: {}", userId);
        return Optional.of(applicationDetailDTO);
    }


    // --- Helper Methods ---

    // Manual mapping for Document (Consider MapStruct) - Copied/adapted from DocumentServiceImpl
    private DocumentDTO mapDocumentEntityToDto(Document entity) {
        return DocumentDTO.builder()
                .id(entity.getId())
                .documentType(entity.getDocumentType())
                .originalFilename(entity.getOriginalFilename())
                .fileSize(entity.getFileSize())
                .contentType(entity.getContentType())
                .status(entity.getStatus())
                .uploadedAt(entity.getUploadedAt())
                .validatedAt(entity.getValidatedAt())
                .validationNotes(entity.getValidationNotes())
                .build();
    }

    @Override
    @Transactional
    public DocumentDTO updateDocumentStatus(Long documentId, DocumentStatusUpdateDTO statusUpdateDTO) {
        log.info("Attempting to update status for document ID: {} to {}", documentId, statusUpdateDTO.getNewStatus());

        if (statusUpdateDTO.getNewStatus() == null) {
            throw new IllegalArgumentException("New status cannot be null.");
        }

        // Find the document
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        // Optional: Add logic here to check for valid status transitions if needed
        // e.g., cannot go from REJECTED back to UPLOADED directly by admin action
        DocumentStatus oldStatus = document.getStatus();
        if (!isValidStatusTransition(oldStatus, statusUpdateDTO.getNewStatus())) {
             log.warn("Invalid status transition requested for document ID {}: from {} to {}",
                      documentId, oldStatus, statusUpdateDTO.getNewStatus());
             throw new IllegalArgumentException("Invalid status transition from " + oldStatus + " to " + statusUpdateDTO.getNewStatus());
        }


        // Update status and notes
        document.setStatus(statusUpdateDTO.getNewStatus());
        if (StringUtils.hasText(statusUpdateDTO.getValidationNotes())) {
            document.setValidationNotes(statusUpdateDTO.getValidationNotes());
        } else {
             // Clear notes if new status is not REJECTED/VALIDATION_FAILED and no notes provided
             if (statusUpdateDTO.getNewStatus() != DocumentStatus.REJECTED && statusUpdateDTO.getNewStatus() != DocumentStatus.VALIDATION_FAILED) {
                  document.setValidationNotes(null);
             }
        }
        // validatedAt timestamp is updated automatically by @PreUpdate if status is VALIDATED/REJECTED

        Document updatedDocument = documentRepository.save(document);
        log.info("Successfully updated status for document ID: {} to {}", documentId, updatedDocument.getStatus());

        // Send notification email to the user asynchronously
        sendDocumentStatusUpdateEmail(updatedDocument);

        return mapDocumentEntityToDto(updatedDocument);
    }


    // --- Helper Methods ---

    private boolean isValidStatusTransition(DocumentStatus oldStatus, DocumentStatus newStatus) {
        // Implement specific rules here. Example: Allow most transitions by admin,
        // but maybe prevent going back from a final state without specific logic.
        if (oldStatus == newStatus) return true; // No change is valid

        // Example: Allow admin to set VALIDATED or REJECTED from most states
        if (newStatus == DocumentStatus.VALIDATED || newStatus == DocumentStatus.REJECTED) {
            return oldStatus == DocumentStatus.UPLOADED || oldStatus == DocumentStatus.VALIDATION_PENDING || oldStatus == DocumentStatus.VALIDATION_FAILED;
        }
        // Add more rules as needed
        log.warn("Status transition check: From {} to {} - currently allowed.", oldStatus, newStatus);
        return true; // Default allow for now, refine later
    }

    private void sendDocumentStatusUpdateEmail(Document document) {
        try {
            User user = document.getUser();
            String subject = String.format("Update on your document: %s", document.getDocumentType().getDescription());
            String templateName = "document-status-update"; // Name of the email template file (e.g., document-status-update.mjml)

            Map<String, Object> templateModel = Map.of(
                    "userName", user.getFirstName(),
                    "documentType", document.getDocumentType().getDescription(),
                    "newStatus", document.getStatus().toString(),
                    "validationNotes", StringUtils.hasText(document.getValidationNotes()) ? document.getValidationNotes() : "N/A",
                    "applicationUrl", "http://localhost:4200/applicant/dashboard" // Replace with actual frontend URL
            );

            emailService.sendMessageUsingTemplate(user.getEmail(), subject, templateName, templateModel);
            log.info("Document status update email queued for user ID: {}, document ID: {}", user.getId(), document.getId());
        } catch (Exception e) {
            // Log error but don't fail the status update process if email fails
            log.error("Failed to send document status update email for document ID {}: {}", document.getId(), e.getMessage());
        }
    }

    // NOTE: Removed duplicate mapDocumentEntityToDto method here. The one at lines 88-101 is kept.

    // Implementations for other admin methods will go here later

    @Override
    @Transactional
    public UserSummaryDTO updateUserApplicationStatus(Long userId, com.registration.platform.model.entity.ApplicationStatus newStatus) {
        log.info("Attempting to update application status for user ID: {} to {}", userId, newStatus);

        if (newStatus == null) {
            throw new IllegalArgumentException("New application status cannot be null.");
        }

        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Optional: Add logic here to check for valid status transitions if needed
        // e.g., cannot go from REJECTED back to PENDING without specific action
        com.registration.platform.model.entity.ApplicationStatus oldStatus = user.getApplicationStatus();
        if (oldStatus == newStatus) {
             log.debug("Application status for user ID {} is already {}. No change needed.", userId, newStatus);
             return UserSummaryDTO.fromUser(user); // Return current state if no change
        }

        // Update status
        user.setApplicationStatus(newStatus);

        User updatedUser = userRepository.save(user);
        log.info("Successfully updated application status for user ID: {} from {} to {}", userId, oldStatus, updatedUser.getApplicationStatus());

        // Optional: Send notification email to the user about application status change
        // sendApplicationStatusUpdateEmail(updatedUser); // Implement this helper method

        return UserSummaryDTO.fromUser(updatedUser);
    }

    // Helper method to send application status update email (Placeholder)
    // private void sendApplicationStatusUpdateEmail(User user) {
    //     try {
    //         String subject = String.format("Update on your application status: %s", user.getApplicationStatus());
    //         String templateName = "application-status-update"; // Name of the email template file
    //
    //         Map<String, Object> templateModel = Map.of(
    //                 "userName", user.getFirstName(),
    //                 "newStatus", user.getApplicationStatus().toString(),
    //                 "applicationUrl", "http://localhost:4200/applicant/dashboard" // Replace with actual frontend URL
    //         );
    //
    //         emailService.sendMessageUsingTemplate(user.getEmail(), subject, templateName, templateModel);
    //         log.info("Application status update email queued for user ID: {}", user.getId());
    //     } catch (Exception e) {
    //         log.error("Failed to send application status update email for user ID {}: {}", user.getId(), e.getMessage());
    //     }
    // }

    // --- Dashboard Statistics Methods ---

    @Override
    @Transactional(readOnly = true)
    public String getApplicationCompletionRateLast30Days() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        log.debug("Calculating completion rate since: {}", thirtyDaysAgo);

        // Count approved applications within the last 30 days
        long approvedLast30Days = userRepository.countByApplicationStatusAndUpdatedAtAfter(
                ApplicationStatus.APPROVED, thirtyDaysAgo);

        // Count total applications updated (approved or rejected) in the last 30 days
        long rejectedLast30Days = userRepository.countByApplicationStatusAndUpdatedAtAfter(
                ApplicationStatus.REJECTED, thirtyDaysAgo);

        long pendingLast30Days = userRepository.countByApplicationStatusAndUpdatedAtAfter(
                ApplicationStatus.PENDING, thirtyDaysAgo);

        long totalProcessedLast30Days = approvedLast30Days + rejectedLast30Days + pendingLast30Days;

        if (totalProcessedLast30Days == 0) {
            log.info("No applications processed in the last 30 days.");
            return "0.0"; // Avoid division by zero
        }

        double rate = ((double) approvedLast30Days / totalProcessedLast30Days) * 100.0;
        String formattedRate = String.format("%.2f", rate); // Format to two decimal places
        log.info("Application completion rate (last 30 days): {}%", formattedRate);
        return formattedRate; // Return the formatted string
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalApplicationsCount() {
        // Count users with the ROLE_APPLICANT
        long count = userRepository.countByRolesContaining(Role.ROLE_APPLICANT);
        log.debug("Total applications count (ROLE_APPLICANT): {}", count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public long getPendingApplicationsCount() {
        // Count users with ROLE_APPLICANT and PENDING status
        long count = userRepository.countByRoleAndApplicationStatus(Role.ROLE_APPLICANT, ApplicationStatus.PENDING);
        log.debug("Pending applications count: {}", count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public long getRejectedApplicationsCount() {
         // Count users with ROLE_APPLICANT and REJECTED status
        long count = userRepository.countByRoleAndApplicationStatus(Role.ROLE_APPLICANT, ApplicationStatus.REJECTED);
        log.debug("Rejected applications count: {}", count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public long getApprovedApplicationsCount() {
         // Count users with ROLE_APPLICANT and REJECTED status
        long count = userRepository.countByRoleAndApplicationStatus(Role.ROLE_APPLICANT, ApplicationStatus.APPROVED);
        log.debug("Rejected applications count: {}", count);
        return count;
    }

}