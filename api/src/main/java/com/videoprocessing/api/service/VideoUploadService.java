package com.videoprocessing.api.service;

import com.videoprocessing.api.dto.VideoUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface VideoUploadService {

    VideoUploadResponse uploadVideo(MultipartFile file);
}
