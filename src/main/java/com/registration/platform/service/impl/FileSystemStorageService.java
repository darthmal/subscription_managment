package com.registration.platform.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.registration.platform.config.FileStorageProperties;
import com.registration.platform.exception.FileNotFoundException;
import com.registration.platform.exception.FileStorageException;
import com.registration.platform.service.FileStorageService;

import jakarta.annotation.PostConstruct;

@Service
public class FileSystemStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileSystemStorageService.class);
    private final Path rootLocation;

    public FileSystemStorageService(FileStorageProperties properties) {
        if (properties.getUploadDir() == null || properties.getUploadDir().isBlank()) {
            log.error("File upload location cannot be empty.");
            throw new FileStorageException("File upload location cannot be empty.");
        }
        // Ensure the root location is an absolute and normalized path
        this.rootLocation = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
        log.info("File storage root location set to: {}", this.rootLocation); // Log the normalized absolute path
    }

    @Override
    @PostConstruct // Ensure init() is called after dependency injection
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            log.info("Initialized file storage directory: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            log.error("Could not initialize storage location: {}", rootLocation.toAbsolutePath(), e);
            throw new FileStorageException("Could not initialize storage", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String... subDirectoryParts) {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename; // Create unique name
        try {
            if (file.isEmpty()) {
                throw new FileStorageException("Failed to store empty file " + originalFilename);
            }
            if (originalFilename.contains("..")) {
                // This is a security check
                throw new FileStorageException("Cannot store file with relative path outside current directory " + originalFilename);
            }

            // Resolve the target directory (root + subdirectories)
            Path targetDirectory = this.rootLocation;
            if (subDirectoryParts != null && subDirectoryParts.length > 0) {
                // Sanitize subdirectory parts as well
                for (String part : subDirectoryParts) {
                     if (part == null || part.contains("..") || part.contains("/") || part.contains("\\")) {
                         throw new FileStorageException("Invalid subdirectory part: " + part);
                     }
                     targetDirectory = targetDirectory.resolve(part);
                }
            }

            // Ensure target directory exists
            Files.createDirectories(targetDirectory);

            Path destinationFile = targetDirectory.resolve(uniqueFilename).normalize().toAbsolutePath();
            Path absolutePAth = this.rootLocation.toAbsolutePath();
            // Ensure the destination is within the root location (extra security)
//            if (!destinationFile.getParent().startsWith(this.rootLocation.toAbsolutePath())) {
//                 throw new FileStorageException("Cannot store file outside root storage directory.");
//            }

            log.debug("Attempting to store file at: {}", destinationFile);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                log.info("Successfully stored file: {} at path: {}", uniqueFilename, destinationFile);
            }

            // Return the path relative to the root location, including subdirs
            Path relativePath = this.rootLocation.relativize(destinationFile);
            return relativePath.toString().replace("\\", "/"); // Use forward slashes for consistency

        } catch (IOException e) {
            log.error("Failed to store file {}: {}", uniqueFilename, e.getMessage(), e);
            throw new FileStorageException("Failed to store file " + uniqueFilename, e);
        }
    }


    @Override
    public Stream<Path> loadAll(String... subDirectoryParts) {
        Path calculatedSourceDirectory = this.rootLocation; // Start with root
        if (subDirectoryParts != null && subDirectoryParts.length > 0) {
            for (String part : subDirectoryParts) {
                // Resolve parts one by one
                calculatedSourceDirectory = calculatedSourceDirectory.resolve(part);
            }
        }

        // Use a final variable for the lambda expressions
        final Path finalSourceDirectory = calculatedSourceDirectory;

        try {
            // Ensure the directory exists before walking
            if (!Files.exists(finalSourceDirectory) || !Files.isDirectory(finalSourceDirectory)) {
                 log.warn("Attempted to load files from non-existent or non-directory path: {}", finalSourceDirectory.toAbsolutePath());
                 return Stream.empty(); // Return an empty stream if directory doesn't exist
            }

            log.debug("Loading all files from directory: {}", finalSourceDirectory.toAbsolutePath());
            return Files.walk(finalSourceDirectory, 1) // Walk only one level deep
                    .filter(path -> !path.equals(finalSourceDirectory)) // Exclude the directory itself
                    .map(finalSourceDirectory::relativize); // Return paths relative to the final source directory
        } catch (IOException e) {
            log.error("Failed to read stored files from {}: {}", finalSourceDirectory.toAbsolutePath(), e.getMessage(), e);
            throw new FileStorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename) {
        // Filename might include subdirectories relative to root
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                log.debug("Loading resource: {}", filename);
                return resource;
            } else {
                log.warn("Could not read file: {}", filename);
                throw new FileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            log.error("Malformed URL for file {}: {}", filename, e.getMessage(), e);
            throw new FileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteFile(String filename) {
        try {
            Path file = load(filename);
            boolean deleted = Files.deleteIfExists(file);
            if (deleted) {
                log.info("Successfully deleted file: {}", filename);
            } else {
                 log.warn("Attempted to delete non-existent file: {}", filename);
                 // Optionally throw FileNotFoundException here if required
                 // throw new FileNotFoundException("File not found: " + filename);
            }
        } catch (IOException e) {
            log.error("Could not delete file {}: {}", filename, e.getMessage(), e);
            throw new FileStorageException("Could not delete file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        log.warn("Deleting all files in storage directory: {}", rootLocation.toAbsolutePath());
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
        // Re-initialize the directory after deleting
        init();
    }
}