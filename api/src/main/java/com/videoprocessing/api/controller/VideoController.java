package com.videoprocessing.api.controller;

import com.videoprocessing.api.dto.ApiResponse;
import com.videoprocessing.api.dto.JobDetailResponse;
import com.videoprocessing.api.dto.JobListResponse;
import com.videoprocessing.api.dto.JobStatusResponse;
import com.videoprocessing.api.dto.VideoUploadResponse;
import com.videoprocessing.api.service.VideoQueryService;
import com.videoprocessing.api.service.VideoUploadService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoUploadService videoUploadService;
    private final VideoQueryService videoQueryService;

    public VideoController(
            VideoUploadService videoUploadService,
            VideoQueryService videoQueryService
    ) {
        this.videoUploadService = videoUploadService;
        this.videoQueryService = videoQueryService;
    }

    @Tag(
            name = "Video Processing",
            description = "APIs for uploading and processing videos"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "202",
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
            @Valid @RequestParam("file") MultipartFile file
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

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<List<JobListResponse>>> listJobs() {
        List<JobListResponse> jobs = videoQueryService.listJobs();
        return ResponseEntity.ok(new ApiResponse<>(true, jobs, "Jobs retrieved successfully"));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ApiResponse<JobDetailResponse>> getJobDetail(
            @PathVariable UUID jobId
    ) {
        JobDetailResponse detail = videoQueryService.getJobDetail(jobId);
        return ResponseEntity.ok(new ApiResponse<>(true, detail, "Job details retrieved"));
    }

    @GetMapping("/jobs/{jobId}/status")
    public ResponseEntity<ApiResponse<JobStatusResponse>> getJobStatus(
            @PathVariable UUID jobId
    ) {
        JobStatusResponse status = videoQueryService.getJobStatus(jobId);
        return ResponseEntity.ok(new ApiResponse<>(true, status, "Job status retrieved"));
    }
}
