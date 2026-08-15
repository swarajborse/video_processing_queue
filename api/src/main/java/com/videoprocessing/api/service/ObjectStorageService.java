package com.videoprocessing.api.service;

import org.springframework.web.multipart.MultipartFile;

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
}