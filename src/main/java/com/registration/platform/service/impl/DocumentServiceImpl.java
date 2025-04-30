package com.registration.platform.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.registration.platform.exception.FileNotFoundException;
import com.registration.platform.exception.FileStorageException;
import com.registration.platform.model.dto.DocumentDTO;
import com.registration.platform.model.entity.Document;
import com.registration.platform.model.entity.DocumentStatus;
import com.registration.platform.model.entity.DocumentType;
import com.registration.platform.model.entity.User;
import com.registration.platform.repository.DocumentRepository;
import com.registration.platform.repository.UserRepository;
import com.registration.platform.service.DocumentService;
import com.registration.platform.service.FileStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public DocumentDTO storeDocument(MultipartFile file, DocumentType documentType, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        log.info("Attempting to store document type '{}' for user ID: {}", documentType, currentUser.getId());

        // 1. Validate file against DocumentType constraints
        validateFile(file, documentType);

        // 2. Check if non-repeatable document type already exists (optional, based on specific types)
        // Example: Only allow one ID_PHOTO
        if (documentType == DocumentType.ID_PHOTO) {
            documentRepository.findByUserAndDocumentType(currentUser, documentType).ifPresent(existingDoc -> {
                log.warn("User {} already has an ID_PHOTO (ID: {}). Replacing is not implemented by default.", currentUser.getEmail(), existingDoc.getId());
                // Decide on replacement strategy: delete old, update existing, or throw error.
                // For now, we'll throw an error to prevent duplicates of non-repeatable types.
                throw new IllegalArgumentException("Document type '" + documentType.getDescription() + "' already exists for this user.");
                // Alternatively, delete old one first: deleteDocumentInternal(existingDoc);
            });
        }
        // Add similar checks for other non-repeatable types if needed.

        // 3. Store the physical file using FileStorageService
        // Store in a subdirectory named after the user's ID for organization
        String storedFilename = fileStorageService.storeFile(file, String.valueOf(currentUser.getId()));
        log.debug("File stored physically with name: {}", storedFilename);

        // 4. Create and save Document entity metadata
        Document document = Document.builder()
                .user(currentUser)
                .documentType(documentType)
                .originalFilename(file.getOriginalFilename())
                .storedFilename(storedFilename) // Relative path including user ID subdir
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .status(DocumentStatus.UPLOADED) // Initial status
                .build();

        Document savedDocument = documentRepository.save(document);
        log.info("Saved document metadata with ID: {}", savedDocument.getId());

        // 5. Map to DTO and return
        return mapEntityToDto(savedDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentDTO> getUserDocuments(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        log.debug("Fetching documents for user ID: {}", currentUser.getId());
        return documentRepository.findByUserOrderByUploadedAtDesc(currentUser)
                .stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDTO getDocumentMetadata(Long documentId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        log.debug("Fetching document metadata for ID: {} for user ID: {}", documentId, currentUser.getId());
        Document document = findDocumentByIdAndUser(documentId, currentUser);
        return mapEntityToDto(document);
    }


    @Override
    @Transactional(readOnly = true)
    public Resource loadDocumentAsResource(Long documentId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        log.info("Attempting to load resource for document ID: {} for user ID: {}", documentId, currentUser.getId());
        Document document = findDocumentByIdAndUser(documentId, currentUser);
        log.debug("Loading resource from stored path: {}", document.getStoredFilename());
        return fileStorageService.loadAsResource(document.getStoredFilename());
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        log.info("Attempting to delete document ID: {} for user ID: {}", documentId, currentUser.getId());
        Document document = findDocumentByIdAndUser(documentId, currentUser);
        deleteDocumentInternal(document);
    }

    // --- Helper Methods ---

    private void deleteDocumentInternal(Document document) {
        String storedFilename = document.getStoredFilename();
        try {
            // 1. Delete physical file
            fileStorageService.deleteFile(storedFilename);
            log.debug("Deleted physical file: {}", storedFilename);
            // 2. Delete metadata record
            documentRepository.delete(document);
            log.info("Deleted document metadata with ID: {}", document.getId());
        } catch (FileNotFoundException e) {
            // If physical file is already gone, log warning but still delete metadata
            log.warn("Physical file not found during deletion: {}. Proceeding to delete metadata.", storedFilename, e);
            documentRepository.delete(document);
             log.info("Deleted document metadata with ID: {} despite missing physical file.", document.getId());
        } catch (FileStorageException e) {
            log.error("Error deleting physical file {}: {}", storedFilename, e.getMessage(), e);
            // Rethrow or handle appropriately - might leave orphaned metadata if deletion fails here
            throw e;
        }
    }


    private Document findDocumentByIdAndUser(Long documentId, User user) {
        return documentRepository.findById(documentId)
                .filter(doc -> Objects.equals(doc.getUser().getId(), user.getId())) // Ensure document belongs to the user
                .orElseThrow(() -> {
                    log.warn("Document not found or access denied for ID: {} and user ID: {}", documentId, user.getId());
                    return new FileNotFoundException("Document not found with ID: " + documentId);
                });
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User must be authenticated to perform this operation.");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    private void validateFile(MultipartFile file, DocumentType documentType) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty.");
        }
        // Check Content Type
        String contentType = file.getContentType();
        if (!documentType.isContentTypeAllowed(contentType)) {
            log.warn("Invalid content type '{}' for document type '{}'. Allowed: {}", contentType, documentType, documentType.getAllowedContentTypes());
            throw new IllegalArgumentException("Invalid file type for " + documentType.getDescription() + ". Allowed types: " + String.join(", ", documentType.getAllowedContentTypes()));
        }
        // Check Size
        if (file.getSize() > documentType.getMaxSizeInBytes()) {
            log.warn("File size {} exceeds maximum {} for document type '{}'", file.getSize(), documentType.getMaxSizeInBytes(), documentType);
            throw new IllegalArgumentException("File size exceeds the limit of " + (documentType.getMaxSizeInBytes() / 1024 / 1024) + "MB for " + documentType.getDescription());
        }
        log.debug("File validation passed for type '{}': ContentType='{}', Size={}", documentType, contentType, file.getSize());
    }

    // Manual mapping (Consider MapStruct)
    private DocumentDTO mapEntityToDto(Document entity) {
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
}