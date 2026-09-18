package com.videoprocessing.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RetryBackoffCalculator {

    private final long initialDelaySeconds;
    private final long maxDelaySeconds;

    public RetryBackoffCalculator(
            @Value("${video.processing.retry.initial-delay-seconds}")
            long initialDelaySeconds,

            @Value("${video.processing.retry.max-delay-seconds}")
            long maxDelaySeconds
    ) {
        this.initialDelaySeconds = initialDelaySeconds;
        this.maxDelaySeconds = maxDelaySeconds;
    }

    public long calculateDelay(int retryCount) {

        long delay =
                initialDelaySeconds *
                        (1L << (retryCount - 1));

        return Math.min(
                delay,
                maxDelaySeconds
        );
    }
}