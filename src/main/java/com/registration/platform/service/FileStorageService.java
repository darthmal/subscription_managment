package com.registration.platform.service;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Initializes the storage location.
     */
    void init();

    /**
     * Stores a file. The implementation should handle generating a unique filename
     * or organizing files (e.g., in subdirectories per user or application).
     *
     * @param file The file uploaded by the user.
     * @param subDirectory Optional subdirectory within the main upload directory (e.g., user ID).
     * @return The unique filename (or path relative to the upload root) under which the file was stored.
     * @throws com.registration.platform.exception.FileStorageException if storing fails.
     */
    String storeFile(MultipartFile file, String... subDirectory);

    /**
     * Loads all filenames within the storage location (or a specific subdirectory).
     *
     * @param subDirectory Optional subdirectory to list files from.
     * @return A Stream of Paths.
     */
    Stream<Path> loadAll(String... subDirectory);

    /**
     * Loads a file as a Path.
     *
     * @param filename The name of the file to load (potentially including subdirectories).
     * @return The Path object for the file.
     */
    Path load(String filename);

    /**
     * Loads a file as a Resource.
     *
     * @param filename The name of the file to load (potentially including subdirectories).
     * @return The Resource object for the file.
     * @throws com.registration.platform.exception.FileNotFoundException if the file does not exist.
     */
    Resource loadAsResource(String filename);

    /**
     * Deletes a file.
     *
     * @param filename The name of the file to delete (potentially including subdirectories).
     * @throws com.registration.platform.exception.FileStorageException if deletion fails.
     * @throws com.registration.platform.exception.FileNotFoundException if the file does not exist.
     */
    void deleteFile(String filename);

    /**
     * Deletes all files in the storage location (use with caution!).
     */
    void deleteAll();

}