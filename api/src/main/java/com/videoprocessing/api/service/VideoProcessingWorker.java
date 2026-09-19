package com.videoprocessing.api.service;

import com.videoprocessing.api.entity.ProcessingJob;
import com.videoprocessing.api.entity.ProcessingJobStatus;
import com.videoprocessing.api.entity.ProcessingOutput;
import com.videoprocessing.api.entity.ProcessingOutputType;
import com.videoprocessing.api.enum_.VideoResolution;
import com.videoprocessing.api.event.VideoProcessingEvent;
import com.videoprocessing.api.exception.ResourceNotFoundException;
import com.videoprocessing.api.metrics.VideoProcessingMetrics;
import com.videoprocessing.api.model.VideoMetadata;
import com.videoprocessing.api.repository.ProcessingJobRepository;
import com.videoprocessing.api.repository.ProcessingOutputRepository;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Component
public class VideoProcessingWorker {

    private static final Logger log =
            LoggerFactory.getLogger(VideoProcessingWorker.class);

    private final ObjectStorageService objectStorageService;
    private final ProcessingJobRepository processingJobRepository;
    private final ProcessingOutputRepository processingOutputRepository;
    private final ProcessingWorkspaceManager workspaceManager;
    private final FFmpegService ffmpegService;
    private final ResolutionSelector resolutionSelector;
    private final FFprobeService ffprobeService;
    private final OutputKeyGenerator outputKeyGenerator;
    private final RetryHandler retryHandler;
    private final KafkaVideoProcessingEventPublisher eventPublisher;
    private final DeadLetterEventPublisher deadLetterEventPublisher;
    private final RetryBackoffCalculator retryBackoffCalculator;
    private final VideoProcessingMetrics metrics;
    public VideoProcessingWorker(
            ObjectStorageService objectStorageService,
            ProcessingJobRepository processingJobRepository,
            ProcessingOutputRepository processingOutputRepository,
            ProcessingWorkspaceManager workspaceManager,
            FFmpegService ffmpegService,
            ResolutionSelector resolutionSelector,
            FFprobeService ffprobeService,
            OutputKeyGenerator outputKeyGenerator,
            RetryHandler retryHandler,
            KafkaVideoProcessingEventPublisher eventPublisher,
            DeadLetterEventPublisher deadLetterEventPublisher,
            RetryBackoffCalculator retryBackoffCalculator,
            VideoProcessingMetrics metrics
    ) {
        this.objectStorageService = objectStorageService;
        this.processingJobRepository = processingJobRepository;
        this.processingOutputRepository = processingOutputRepository;
        this.workspaceManager = workspaceManager;
        this.ffmpegService = ffmpegService;
        this.resolutionSelector = resolutionSelector;
        this.ffprobeService = ffprobeService;
        this.outputKeyGenerator = outputKeyGenerator;
        this.retryHandler = retryHandler;
        this.eventPublisher = eventPublisher;
        this.deadLetterEventPublisher = deadLetterEventPublisher;
        this.retryBackoffCalculator = retryBackoffCalculator;
        this.metrics = metrics;
    }

    private void updateHeartbeat(ProcessingJob job) {
        job.setLastHeartbeatAt(Instant.now());
        processingJobRepository.save(job);
    }

    public void process(VideoProcessingEvent event) {


        Timer.Sample sample = Timer.start();
        metrics.jobStarted();

        log.info("WORKER: Received event for job {}", event.jobId());

        ProcessingJob job =
                processingJobRepository.findById(event.jobId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Processing job not found"
                                )
                        );

        log.info("WORKER: Job {} found, current status={}", job.getId(), job.getStatus());

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

        log.info("WORKER: Job {} marked PROCESSING, creating workspace", job.getId());

        ProcessingWorkspace workspace =
                workspaceManager.create();

        log.info("WORKER: Workspace created for job {}", job.getId());



        try {

            /*
             * Step 1:
             * Download original video from object storage
             */
            log.info("WORKER: [Step 1] Starting S3 download for job {} key={}",
                    job.getId(), event.originalS3Key());

            objectStorageService.download(
                    event.originalS3Key(),
                    workspace.originalVideo()
            );

            log.info("WORKER: [Step 1] S3 download completed for job {}", job.getId());
            updateHeartbeat(job);

            /*
             * Step 2:
             * Read video metadata using FFprobe
             */
            log.info("WORKER: [Step 2] Starting FFprobe for job {}", job.getId());

            VideoMetadata metadata =
                    ffprobeService.probe(
                            workspace.originalVideo()
                    );

            log.info("WORKER: [Step 2] FFprobe completed for job {} — {}x{} duration={}s",
                    job.getId(), metadata.width(), metadata.height(), metadata.duration());

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

                log.info("WORKER: [Step 4] Starting FFmpeg transcode for job {} resolution={}",
                        job.getId(), resolution);

                Path output =
                        workspace.outputPath(resolution);

                ffmpegService.execute(
                        workspace.originalVideo(),
                        output,
                        resolution
                );

                log.info("WORKER: [Step 4] FFmpeg transcode done for job {} resolution={}",
                        job.getId(), resolution);

                String s3Key =
                        outputKeyGenerator.outputKey(
                                event.videoId(),
                                resolution
                        );

                log.info("WORKER: [Step 4] Uploading transcoded output for job {} key={}",
                        job.getId(), s3Key);

                objectStorageService.upload(
                        output,
                        s3Key
                );

                log.info("WORKER: [Step 4] Upload done for job {} resolution={}",
                        job.getId(), resolution);

                updateHeartbeat(job);

                /*
                 * Save output information in database
                 */
                ProcessingOutput processingOutput =
                        new ProcessingOutput();

                processingOutput.setJob(job);
                processingOutput.setType(ProcessingOutputType.VIDEO);
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
            log.info("WORKER: [Step 5] Generating thumbnail for job {}", job.getId());

            Path thumbnail =
                    workspace.thumbnailPath();

            ffmpegService.generateThumbnail(
                    workspace.originalVideo(),
                    thumbnail
            );

            log.info("WORKER: [Step 5] Thumbnail generated, uploading for job {}", job.getId());

            String thumbnailKey =
                    outputKeyGenerator.thumbnailKey(
                            event.videoId()
                    );

            objectStorageService.upload(
                    thumbnail,
                    thumbnailKey
            );

            log.info("WORKER: [Step 5] Thumbnail uploaded for job {}", job.getId());

            /*
             * Save thumbnail information
             */
            ProcessingOutput thumbnailOutput =
                    new ProcessingOutput();

            thumbnailOutput.setJob(job);
            thumbnailOutput.setType(ProcessingOutputType.THUMBNAIL);
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
            job.setCompletedAt(Instant.now());
            job.setProgress(100);
            job.setStatus(ProcessingJobStatus.COMPLETED);
            metrics.recordProcessed();

            processingJobRepository.save(job);

            log.info("WORKER: Job {} COMPLETED successfully", job.getId());

        } catch (Exception e) {

            log.error("WORKER: Job {} failed with exception: {}",
                    job.getId(), e.getMessage(), e);

            boolean retryable = retryHandler.isRetryable(e);
            boolean canRetry = retryHandler.canRetry(job);

            String errorMessage =
                    e.getMessage() != null
                            ? e.getMessage()
                            : e.getClass().getSimpleName();

            job.setLastError(errorMessage);

            if (retryable && canRetry) {

                int nextRetryCount = job.getRetryCount() + 1;

                long delaySeconds =
                        retryBackoffCalculator.calculateDelay(nextRetryCount);

                job.setRetryCount(nextRetryCount);

                job.setNextRetryAt(
                        Instant.now().plusSeconds(delaySeconds)
                );

                job.setStatus(ProcessingJobStatus.QUEUED);

                processingJobRepository.save(job);

                return;
            }

            job.setStatus(ProcessingJobStatus.FAILED);
            metrics.recordFailed();

            processingJobRepository.save(job);

            deadLetterEventPublisher.publish(event);
        } finally {

            /*
             * Always remove temporary files
             */
            metrics.jobFinished();

            sample.stop(metrics.processingDuration());
            workspaceManager.cleanup(workspace);
        }
    }
}