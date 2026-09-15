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

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Component
public class VideoProcessingWorker {

    private final ObjectStorageService objectStorageService;
    private final ProcessingJobRepository processingJobRepository;
    private final ProcessingOutputRepository processingOutputRepository;
    private final ProcessingWorkspaceManager workspaceManager;
    private final FFmpegService ffmpegService;
    private final ResolutionSelector resolutionSelector;
    private final FFprobeService ffprobeService;
    private final OutputKeyGenerator outputKeyGenerator;

    public VideoProcessingWorker(
            ObjectStorageService objectStorageService,
            ProcessingJobRepository processingJobRepository,
            ProcessingOutputRepository processingOutputRepository,
            ProcessingWorkspaceManager workspaceManager,
            FFmpegService ffmpegService,
            ResolutionSelector resolutionSelector,
            FFprobeService ffprobeService,
            OutputKeyGenerator outputKeyGenerator
    ) {
        this.objectStorageService = objectStorageService;
        this.processingJobRepository = processingJobRepository;
        this.processingOutputRepository = processingOutputRepository;
        this.workspaceManager = workspaceManager;
        this.ffmpegService = ffmpegService;
        this.resolutionSelector = resolutionSelector;
        this.ffprobeService = ffprobeService;
        this.outputKeyGenerator = outputKeyGenerator;
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

        /*
         * Mark job as PROCESSING
         */
        Instant now = Instant.now();

        job.setStatus(ProcessingJobStatus.PROCESSING);
        job.setStartedAt(now);
        job.setLastHeartbeatAt(now);
        job.setProgress(0);
        job.setLastError(null);

        processingJobRepository.save(job);

        private void updateHeartbeat(ProcessingJob job) {
            job.setLastHeartbeatAt(Instant.now());
            processingJobRepository.save(job);
        }

        ProcessingWorkspace workspace =
                workspaceManager.create();

        try {

            /*
             * Step 1:
             * Download original video from object storage
             */
            objectStorageService.download(
                    event.originalS3Key(),
                    workspace.originalVideo()
            );
            updateHeartbeat(job);

            /*
             * Step 2:
             * Read video metadata using FFprobe
             */
            VideoMetadata metadata =
                    ffprobeService.probe(
                            workspace.originalVideo()
                    );



            updateHeartbeat(job);
            /*


             * Step 3:
             * Decide which resolutions can be generated
             */
            List<VideoResolution> resolutions =
                    resolutionSelector.select(metadata);

            /*
             * Step 4:
             * Generate and upload processed videos
             */
            for (VideoResolution resolution : resolutions) {

                Path output =
                        workspace.outputPath(resolution);

                ffmpegService.execute(
                        workspace.originalVideo(),
                        output,
                        resolution
                );

                String s3Key =
                        outputKeyGenerator.outputKey(
                                event.videoId(),
                                resolution
                        );

                objectStorageService.upload(
                        output,
                        s3Key
                );


                updateHeartbeat(job);

                /*
                 * Save output information in database
                 */
                ProcessingOutput processingOutput =
                        new ProcessingOutput();

                processingOutput.setJob(job);
                processingOutput.setResolution(
                        resolution.height() + "p"
                );
                processingOutput.setS3Key(s3Key);

                processingOutputRepository.save(
                        processingOutput
                );


                updateHeartbeat(job);




            }

            /*
             * Step 5:
             * Generate and upload thumbnail
             */
            Path thumbnail =
                    workspace.thumbnailPath();

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

            /*
             * Save thumbnail information
             */
            ProcessingOutput thumbnailOutput =
                    new ProcessingOutput();

            thumbnailOutput.setJob(job);
            thumbnailOutput.setResolution("thumbnail");
            thumbnailOutput.setS3Key(thumbnailKey);

            processingOutputRepository.save(
                    thumbnailOutput
            );

            /*
             * Step 6:
             * Save original video metadata
             */
            job.setWidth(metadata.width());
            job.setHeight(metadata.height());
            job.setDuration(metadata.duration());

            /*
             * Step 7:
             * Mark job as successfully completed
             */
            job.setProgress(100);
            job.setStatus(ProcessingJobStatus.COMPLETED);

            processingJobRepository.save(job);

        } catch (Exception e) {

            /*
             * Processing failed
             */
            job.setStatus(ProcessingJobStatus.FAILED);

            String errorMessage =
                    e.getMessage() != null
                            ? e.getMessage()
                            : e.getClass().getSimpleName();

            job.setLastError(errorMessage);

            processingJobRepository.save(job);

            /*
             * Re-throw the exception so the
             * message-processing layer knows
             * that processing failed.
             */
            throw e;

        } finally {

            /*
             * Always remove temporary files
             */
            workspaceManager.cleanup(workspace);
        }
    }
}