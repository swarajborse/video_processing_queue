package com.videoprocessing.api.service;

import org.springframework.web.multipart.MultipartFile;

public interface ObjectStorageService {

    String upload(
            MultipartFile file,
            String objectKey
    );
}