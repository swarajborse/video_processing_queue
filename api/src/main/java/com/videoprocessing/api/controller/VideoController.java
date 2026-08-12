package com.videoprocessing.api.controller;

import com.videoprocessing.api.dto.VideoUploadResponse;
import com.videoprocessing.api.service.VideoUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoUploadService videoUploadService;

    public VideoController(VideoUploadService videoUploadService) {
        this.videoUploadService = videoUploadService;
    }

    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<VideoUploadResponse> uploadVideo(
            @RequestParam("file") MultipartFile file
    ) {

        VideoUploadResponse response =
                videoUploadService.uploadVideo(file);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }
}