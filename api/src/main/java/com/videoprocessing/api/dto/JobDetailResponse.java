package com.videoprocessing.api.dto;

import com.videoprocessing.api.entity.ProcessingJobStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JobDetailResponse(
        UUID jobId,
        UUID videoId,
        String filename,
        Long fileSize,
        String contentType,
        ProcessingJobStatus status,
        Integer progress,
        Integer retryCount,
        Integer maxRetries,
        Instant createdAt,
        Instant startedAt,
        Instant completedAt,
        String lastError,
        Integer width,
        Integer height,
        Double duration,
        List<OutputResponse> outputs
) {}
