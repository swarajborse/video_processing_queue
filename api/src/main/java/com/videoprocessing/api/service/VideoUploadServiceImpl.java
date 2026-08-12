package com.videoprocessing.api.service;

import com.videoprocessing.api.dto.VideoUploadResponse;
import com.videoprocessing.api.entity.Video;
import com.videoprocessing.api.entity.VideoStatus;
import com.videoprocessing.api.repository.VideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class VideoUploadServiceImpl implements VideoUploadService {

    private final ObjectStorageService objectStorageService;
    private final VideoRepository videoRepository;

    public VideoUploadServiceImpl(
            ObjectStorageService objectStorageService,
            VideoRepository videoRepository
    ) {
        this.objectStorageService = objectStorageService;
        this.videoRepository = videoRepository;
    }

    @Override
    public VideoUploadResponse uploadVideo(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Video file is required"
            );
        }

        UUID videoId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

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

        Video savedVideo = videoRepository.save(video);

        return new VideoUploadResponse(
                savedVideo.getId(),
                jobId,
                savedVideo.getStatus()
        );
    }
}