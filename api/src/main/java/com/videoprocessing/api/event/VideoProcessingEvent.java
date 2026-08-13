package com.videoprocessing.api.event;

import java.util.UUID;

public record VideoProcessingEvent(
        UUID jobId,
        UUID videoId,
        String originalS3Key
) {
}