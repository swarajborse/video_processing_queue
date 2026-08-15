package com.videoprocessing.api.service;

import com.videoprocessing.api.entity.ProcessingJob;
import com.videoprocessing.api.entity.ProcessingJobStatus;
import com.videoprocessing.api.event.VideoProcessingEvent;
import com.videoprocessing.api.repository.ProcessingJobRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@Component
public class VideoProcessingWorker {

    private final ObjectStorageService objectStorageService;
    private final ProcessingJobRepository processingJobRepository;
    private final ProcessingWorkspaceManager workspaceManager;
    private final FFmpegService ffmpegService;


    public VideoProcessingWorker(
            ObjectStorageService objectStorageService,
            ProcessingJobRepository processingJobRepository,
            ProcessingWorkspaceManager workspaceManager,
            FFmpegService ffmpegService
    ) {
        this.objectStorageService = objectStorageService;
        this.processingJobRepository = processingJobRepository;
        this.workspaceManager = workspaceManager;
        this.ffmpegService=ffmpegService;
    }

    public void process(VideoProcessingEvent event) {

        ProcessingJob job =
                processingJobRepository.findById(event.jobId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Processing job not found: "
                                                + event.jobId()
                                )
                        );

        job.setStatus(
                ProcessingJobStatus.PROCESSING
        );

        job.setStartedAt(Instant.now());
        job.setProgress(0);

        processingJobRepository.save(job);

        ProcessingWorkspace workspace =
                workspaceManager.create();

        try {

            objectStorageService.download(
                    event.originalS3Key(),
                    workspace.originalVideo()
            );

            System.out.println(
                    "Downloaded video to: "
                            + workspace.originalVideo()
            );

            Path processedVideo =
                    workspace.directory()
                            .resolve("processed.mp4");

            ffmpegService.execute(
                    workspace.originalVideo(),
                    processedVideo
            );

        } finally {

            workspaceManager.cleanup(workspace);
        }
    }
}