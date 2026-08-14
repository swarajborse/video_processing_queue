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

import java.util.UUID;


@Service
public class VideoUploadServiceImpl implements VideoUploadService {

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
        this.eventPublisher=eventPublisher;
    }

    @Override
    @Transactional
    public VideoUploadResponse uploadVideo(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidVideoException(
                    "Video file is required"
            );
        }

        UUID videoId = UUID.randomUUID();

        String objectKey =
                "videos/"
                        + videoId
                        + "/original/original.mp4";

        objectStorageService.upload(
                file,
                objectKey
        );

        Video video = new Video();

        video.setId(videoId);
        video.setOriginalFilename(
                file.getOriginalFilename()
        );
        video.setOriginalS3Key(objectKey);
        video.setFileSize(file.getSize());
        video.setContentType(file.getContentType());
        video.setStatus(VideoStatus.PROCESSING);

        Video savedVideo =
                videoRepository.save(video);

        ProcessingJob job = new ProcessingJob();


        job.setVideo(savedVideo);
        job.setStatus(ProcessingJobStatus.QUEUED);
        job.setProgress(0);
        job.setRetryCount(0);
        job.setMaxRetries(3);

        ProcessingJob savedJob =
                processingJobRepository.save(job);

        VideoProcessingEvent event = new VideoProcessingEvent(savedJob.getId(),
                savedVideo.getId(),
                savedVideo.getOriginalS3Key());
        eventPublisher.publish(event);

        return new VideoUploadResponse(
                savedVideo.getId(),
                savedJob.getId(),
                savedVideo.getStatus()
        );
    }
}