package com.videoprocessing.api.dto;

import com.videoprocessing.api.entity.ProcessingOutputType;

import java.time.Instant;
import java.util.UUID;

public record OutputResponse(
        UUID outputId,
        ProcessingOutputType type,
        String resolution,
        Long fileSize,
        String contentType,
        String downloadUrl,
        Instant createdAt
) {}
