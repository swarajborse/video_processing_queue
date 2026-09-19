package com.videoprocessing.api.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;

public interface ObjectStorageService {

    String upload(
            MultipartFile file,
            String objectKey
    );

    void download(
            String objectKey,
            Path destination
    );

    void upload(
            Path file,
            String key
    );

    String generatePresignedDownloadUrl(
            String objectKey,
            int expirationMinutes,
            String filename
    );

    /**
     * Opens a streaming InputStream for the given object key.
     * Caller is responsible for closing the stream.
     */
    InputStream streamObject(String objectKey);
}
