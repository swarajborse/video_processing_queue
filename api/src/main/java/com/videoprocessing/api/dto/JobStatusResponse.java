package com.videoprocessing.api.dto;

import com.videoprocessing.api.entity.ProcessingJobStatus;

import java.util.UUID;

public record JobStatusResponse(
        UUID jobId,
        ProcessingJobStatus status,
        Integer progress,
        String lastError
) {}
