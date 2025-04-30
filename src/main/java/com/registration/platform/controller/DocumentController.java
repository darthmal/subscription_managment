package com.registration.platform.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.registration.platform.exception.FileNotFoundException;
import com.registration.platform.exception.FileStorageException;
import com.registration.platform.model.dto.DocumentDTO;
import com.registration.platform.model.entity.DocumentType;
import com.registration.platform.service.DocumentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/applicant/documents") // Base path for document endpoints
@RequiredArgsConstructor
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentService documentService;

    @PostMapping("/{documentType}")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file,
                                            @PathVariable DocumentType documentType,
                                            Authentication authentication) {
        log.info("Received request to upload document type '{}' for user: {}", documentType, authentication.getName());
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }

        try {
            DocumentDTO savedDocument = documentService.storeDocument(file, documentType, authentication);
            log.info("Document type '{}' uploaded successfully for user: {}, ID: {}", documentType, authentication.getName(), savedDocument.getId());
            // Return 201 Created with the DTO of the saved document
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
        } catch (IllegalArgumentException | FileStorageException e) {
            log.warn("Upload failed for document type '{}', user {}: {}", documentType, authentication.getName(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error uploading document type '{}' for user {}: {}", documentType, authentication.getName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during file upload.");
        }
    }

    @GetMapping
    public ResponseEntity<List<DocumentDTO>> listUserDocuments(Authentication authentication) {
        log.debug("Received request to list documents for user: {}", authentication.getName());
        try {
            List<DocumentDTO> documents = documentService.getUserDocuments(authentication);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Error listing documents for user {}: {}", authentication.getName(), e.getMessage(), e);
            // Consider returning an empty list or a specific error status
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve documents", e);
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id, Authentication authentication) {
        log.info("Received request to download document ID: {} for user: {}", id, authentication.getName());
        try {
            Resource resource = documentService.loadDocumentAsResource(id, authentication);
            // Try to determine content type (optional, can fallback)
            String contentType = null; // Determine based on resource.getFilename() or stored metadata if needed
            // For simplicity, using octet-stream as default
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

            // Retrieve original filename from metadata to suggest to browser
            DocumentDTO metadata = documentService.getDocumentMetadata(id, authentication);

            log.info("Serving document ID: {} with original filename: {}", id, metadata.getOriginalFilename());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    // Suggest original filename for download
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getOriginalFilename() + "\"")
                    .body(resource);
        } catch (FileNotFoundException e) {
            log.warn("Download failed for document ID: {}, user {}: {}", id, authentication.getName(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error downloading document ID: {} for user {}: {}", id, authentication.getName(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not download the file", e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id, Authentication authentication) {
        log.info("Received request to delete document ID: {} for user: {}", id, authentication.getName());
        try {
            documentService.deleteDocument(id, authentication);
            log.info("Document ID: {} deleted successfully for user: {}", id, authentication.getName());
            return ResponseEntity.ok("Document deleted successfully."); // Or ResponseEntity.noContent().build();
        } catch (FileNotFoundException e) {
            log.warn("Deletion failed for document ID: {}, user {}: {}", id, authentication.getName(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (FileStorageException e) {
             log.error("Error deleting document file for ID: {}, user {}: {}", id, authentication.getName(), e.getMessage(), e);
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not delete document file.", e);
        } catch (Exception e) {
            log.error("Unexpected error deleting document ID: {} for user {}: {}", id, authentication.getName(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not delete the document.", e);
        }
    }
}