package com.videoprocessing.api.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class VideoProcessingMetrics {

    private final Counter processedVideos;

    private final Counter failedVideos;

    private final Timer processingDuration;

    private final AtomicInteger activeJobs = new AtomicInteger(0);

    public VideoProcessingMetrics(MeterRegistry meterRegistry) {

        this.processedVideos = Counter.builder("video_jobs_processed")
                .description("Total number of successfully processed video jobs")
                .register(meterRegistry);

        this.failedVideos = Counter.builder("video_jobs_failed")
                .description("Total number of failed video jobs")
                .register(meterRegistry);

        this.processingDuration = Timer.builder("video_processing_duration")
                .description("Time taken to process a video job")
                .register(meterRegistry);

        Gauge.builder(
                        "video_jobs_active",
                        activeJobs,
                        AtomicInteger::get
                )
                .description("Number of video jobs currently being processed")
                .register(meterRegistry);
    }

    public void recordProcessed() {
        processedVideos.increment();
    }

    public void recordFailed() {
        failedVideos.increment();
    }

    public Timer processingDuration() {
        return processingDuration;
    }

    public void jobStarted() {
        activeJobs.incrementAndGet();
    }

    public void jobFinished() {
        activeJobs.decrementAndGet();
    }
}