package com.registration.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.registration.platform.model.entity.Document;
import com.registration.platform.model.entity.DocumentType;
import com.registration.platform.model.entity.User;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Find all documents uploaded by a specific user
    List<Document> findByUser(User user);

    // Find all documents uploaded by a specific user, ordered by upload time
    List<Document> findByUserOrderByUploadedAtDesc(User user);

    // Find a specific document type for a specific user
    // Useful for checking if a required non-repeatable document exists (e.g., ID_PHOTO)
    Optional<Document> findByUserAndDocumentType(User user, DocumentType documentType);

    // Find documents by user and a list of types (e.g., find all diplomas for a user)
    List<Document> findByUserAndDocumentTypeIn(User user, List<DocumentType> documentTypes);

    // Find a document by its unique stored filename (useful for retrieval/deletion)
    Optional<Document> findByStoredFilename(String storedFilename);

}