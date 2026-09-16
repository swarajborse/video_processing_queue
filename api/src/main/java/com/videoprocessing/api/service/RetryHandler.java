package com.videoprocessing.api.service;

import com.videoprocessing.api.entity.ProcessingJob;
import com.videoprocessing.api.exception.NonRetryableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RetryHandler {

    private final int maxRetries;

    public RetryHandler(
            @Value("${video.processing.max-retries}")
            int maxRetries
    ) {
        this.maxRetries = maxRetries;
    }

    public boolean isRetryable(Throwable exception) {

        return !(exception instanceof NonRetryableException);
    }

    public boolean canRetry(ProcessingJob job) {

        return job.getRetryCount() < maxRetries;
    }
}