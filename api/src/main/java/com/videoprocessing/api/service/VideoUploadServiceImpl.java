package com.videoprocessing.api.service;

import com.videoprocessing.api.dto.VideoUploadResponse;
import com.videoprocessing.api.entity.ProcessingJob;
import com.videoprocessing.api.entity.ProcessingJobStatus;
import com.videoprocessing.api.entity.Video;
import com.videoprocessing.api.entity.VideoStatus;
import com.videoprocessing.api.event.VideoProcessingEvent;
import com.videoprocessing.api.exception.InvalidVideoException;
import com.videoprocessing.api.repository.ProcessingJobRepository;
import com.videoprocessing.api.repository.VideoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class VideoUploadServiceImpl implements VideoUploadService {

    // Allowlist of accepted MIME types — reject anything that is not a video
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "video/mp4",
            "video/mpeg",
            "video/quicktime",
            "video/x-msvideo",   // AVI
            "video/x-matroska",  // MKV
            "video/webm",
            "video/ogg",
            "video/3gpp",
            "video/x-flv"
    );

    private final ObjectStorageService objectStorageService;
    private final VideoRepository videoRepository;
    private final ProcessingJobRepository processingJobRepository;
    private final VideoProcessingEventPublisher eventPublisher;

    public VideoUploadServiceImpl(
            ObjectStorageService objectStorageService,
            VideoRepository videoRepository,
            ProcessingJobRepository processingJobRepository,
            VideoProcessingEventPublisher eventPublisher
    ) {
        this.objectStorageService = objectStorageService;
        this.videoRepository = videoRepository;
        this.processingJobRepository = processingJobRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public VideoUploadResponse uploadVideo(MultipartFile file, String idempotencyKey) {

        // --- Basic presence check ---
        if (file == null || file.isEmpty()) {
            throw new InvalidVideoException("Video file is required");
        }

        // --- File-type validation (allowlist) ---
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidVideoException(
                    "Unsupported file type. Only video files are accepted."
            );
        }

        // --- Idempotency check BEFORE uploading to storage ---
        // Prevents wasting object-storage space when a client resends the same request.
        Optional<ProcessingJob> existingJob =
                processingJobRepository.findByIdempotencyKey(idempotencyKey);

        if (existingJob.isPresent()) {
            ProcessingJob job = existingJob.get();
            return new VideoUploadResponse(
                    job.getVideo().getId(),
                    job.getId(),
                    job.getVideo().getStatus()
            );
        }

        // --- Upload original video to object storage ---
        UUID videoId = UUID.randomUUID();

        String objectKey =
                "videos/"
                        + videoId
                        + "/original/original.mp4";

        objectStorageService.upload(file, objectKey);

        // --- Persist video entity ---
        Video video = new Video();
        video.setId(videoId);
        video.setOriginalFilename(file.getOriginalFilename());
        video.setOriginalS3Key(objectKey);
        video.setFileSize(file.getSize());
        video.setContentType(contentType);
        video.setStatus(VideoStatus.PROCESSING);

        Video savedVideo = videoRepository.save(video);

        // --- Create processing job ---
        ProcessingJob job = new ProcessingJob();
        job.setIdempotencyKey(idempotencyKey);
        job.setVideo(savedVideo);
        job.setStatus(ProcessingJobStatus.QUEUED);
        job.setProgress(0);
        job.setRetryCount(0);
        job.setMaxRetries(3);

        ProcessingJob savedJob = processingJobRepository.save(job);

        // --- Publish Kafka event ---
        VideoProcessingEvent event = new VideoProcessingEvent(
                savedJob.getId(),
                savedVideo.getId(),
                savedVideo.getOriginalS3Key()
        );
        eventPublisher.publish(event);

        return new VideoUploadResponse(
                savedVideo.getId(),
                savedJob.getId(),
                savedVideo.getStatus()
        );
    }
}