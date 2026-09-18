package com.videoprocessing.api.controller;

import com.videoprocessing.api.dto.ApiResponse;
import com.videoprocessing.api.dto.VideoUploadResponse;
import com.videoprocessing.api.service.VideoUploadService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Value;
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

    @Tag(
            name = "Video Processing",
            description = "APIs for uploading and processing videos"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Job created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<ApiResponse<VideoUploadResponse>> uploadVideo(
            @Parameter(
                    description = "Unique key used to prevent duplicate job creation",
                    required = true
            )
            @RequestHeader("Idempotency-Key") String idempotencyKey,
           @Valid  @RequestParam("file") MultipartFile file

    ) {

        VideoUploadResponse response =
                videoUploadService.uploadVideo(file, idempotencyKey);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                        new ApiResponse<>(
                                true,
                                response,
                                "Video upload accepted"
                        )
                );
    }
}