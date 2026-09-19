package com.videoprocessing.api.dto;

import com.videoprocessing.api.entity.ProcessingJobStatus;

import java.time.Instant;
import java.util.UUID;

public record JobListResponse(
        UUID jobId,
        UUID videoId,
        String filename,
        Long fileSize,
        String contentType,
        ProcessingJobStatus status,
        Integer progress,
        Integer retryCount,
        Instant createdAt,
        Instant startedAt,
        Instant completedAt,
        String lastError
) {}
