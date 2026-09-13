package com.videoprocessing.api.service;

import com.videoprocessing.api.entity.ProcessingJob;
import com.videoprocessing.api.entity.ProcessingJobStatus;
import com.videoprocessing.api.entity.ProcessingOutput;
import com.videoprocessing.api.enum_.VideoResolution;
import com.videoprocessing.api.event.VideoProcessingEvent;
import com.videoprocessing.api.model.VideoMetadata;
import com.videoprocessing.api.repository.ProcessingJobRepository;
import com.videoprocessing.api.repository.ProcessingOutputRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Component
public class VideoProcessingWorker {

    private final ObjectStorageService objectStorageService;
    private final ProcessingJobRepository processingJobRepository;
    private final ProcessingWorkspaceManager workspaceManager;
    private final FFmpegService ffmpegService;
    private final ResolutionSelector resolutionSelector;
    private final FFprobeService ffprobeService;
    private final OutputKeyGenerator outputKeyGenerator;
    private final ProcessingOutputRepository processingOutputRepository;



    public VideoProcessingWorker(
            ObjectStorageService objectStorageService,
            ProcessingJobRepository processingJobRepository,
            ProcessingWorkspaceManager workspaceManager,
            FFmpegService ffmpegService,
            ResolutionSelector resolutionSelector,
            FFprobeService ffprobeService,
            OutputKeyGenerator outputKeyGenerator,
            ProcessingOutputRepository processingOutputRepository
    ) {
        this.objectStorageService = objectStorageService;
        this.processingJobRepository = processingJobRepository;
        this.workspaceManager = workspaceManager;
        this.ffmpegService = ffmpegService;
        this.resolutionSelector = resolutionSelector;
        this.ffprobeService = ffprobeService;
        this.outputKeyGenerator = outputKeyGenerator;
        this.processingOutputRepository = processingOutputRepository;
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

// 2. Extract video metadata using FFprobe
            VideoMetadata metadata =
                    ffprobeService.probe(
                            workspace.originalVideo()
                    );

            System.out.println(
                    "Video resolution: "
                            + metadata.width()
                            + "x"
                            + metadata.height()
            );

// 3. Decide which resolutions to generate
            List<VideoResolution> resolutions =
                    resolutionSelector.select(metadata);

// 4. Generate each selected resolution
            for (VideoResolution resolution : resolutions) {

                // Generate local output
                Path output = workspace.outputPath(resolution);

                ffmpegService.execute(
                        workspace.originalVideo(),
                        output,
                        resolution
                );

                // Generate S3 key
                String s3Key =
                        outputKeyGenerator.outputKey(
                                event.videoId(),
                                resolution
                        );

                // Upload processed video to S3
                objectStorageService.upload(
                        output,
                        s3Key
                );


                ProcessingOutput processingOutput = new ProcessingOutput();

                processingOutput.setS3Key(s3Key);
                processingOutput.setResolution(
                        resolution.height() + "p"
                );

                processingOutputRepository.save(processingOutput);
            }


            Path thumbnail = workspace.thumbnailPath();

            ffmpegService.generateThumbnail(
                    workspace.originalVideo(),
                    thumbnail
            );

            String thumbnailKey =
                    outputKeyGenerator.thumbnailKey(
                            event.videoId()
                    );

            objectStorageService.upload(
                    thumbnail,
                    thumbnailKey
            );

            ProcessingOutput thumbnailOutput =
                    new ProcessingOutput();

            thumbnailOutput.setS3Key(thumbnailKey);
            thumbnailOutput.setResolution("thumbnail");

            processingOutputRepository.save(thumbnailOutput);


            // Save video metadata
            job.setWidth(metadata.width());
            job.setHeight(metadata.height());
            job.setDuration(metadata.duration());

            // Processing finished successfully
            job.setProgress(100);
            job.setStatus(ProcessingJobStatus.COMPLETED);

            processingJobRepository.save(job);

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