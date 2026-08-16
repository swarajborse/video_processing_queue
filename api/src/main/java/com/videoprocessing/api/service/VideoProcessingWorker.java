package com.videoprocessing.api.service;

import com.videoprocessing.api.entity.ProcessingJob;
import com.videoprocessing.api.entity.ProcessingJobStatus;
import com.videoprocessing.api.event.VideoProcessingEvent;
import com.videoprocessing.api.repository.ProcessingJobRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
        this.ffmpegService = ffmpegService;
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

            // 1. Download original video
            objectStorageService.download(
                    event.originalS3Key(),
                    workspace.originalVideo()
            );

            System.out.println(
                    "Downloaded video to: "
                            + workspace.originalVideo()
            );

            // 2. Create output path
            Path processedVideo =
                    workspace.directory()
                            .resolve("processed.mp4");

            // 3. Run FFmpeg
            ffmpegService.execute(
                    workspace.originalVideo(),
                    processedVideo
            );

            System.out.println(
                    "Processed video created at: "
                            + processedVideo
            );

        }catch (Exception e) {

            job.setStatus(
                    ProcessingJobStatus.FAILED
            );

            String errorMessage =
                    e.getMessage() != null
                            ? e.getMessage()
                            : e.getClass().getSimpleName();

            job.setLastError(errorMessage);

            processingJobRepository.save(job);

            throw e;

        } finally {

            // 4. Always cleanup local files
            workspaceManager.cleanup(workspace);
        }
    }
}