package com.registration.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.file-storage")
@Getter
@Setter
public class FileStorageProperties {

    private String uploadDir;

    // We can add other storage-related properties here if needed later
    // (e.g., cloud storage credentials, bucket names)
}