package com.videoprocessing.api.service;

import com.videoprocessing.api.dto.JobDetailResponse;
import com.videoprocessing.api.dto.JobListResponse;
import com.videoprocessing.api.dto.JobStatusResponse;
import com.videoprocessing.api.dto.OutputResponse;
import com.videoprocessing.api.entity.ProcessingJob;
import com.videoprocessing.api.entity.ProcessingOutput;
import com.videoprocessing.api.exception.ResourceNotFoundException;
import com.videoprocessing.api.repository.ProcessingJobRepository;
import com.videoprocessing.api.repository.ProcessingOutputRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class VideoQueryServiceImpl implements VideoQueryService {

    private static final int PRESIGNED_URL_EXPIRY_MINUTES = 60;

    private final ProcessingJobRepository processingJobRepository;
    private final ProcessingOutputRepository processingOutputRepository;
    private final ObjectStorageService objectStorageService;

    public VideoQueryServiceImpl(
            ProcessingJobRepository processingJobRepository,
            ProcessingOutputRepository processingOutputRepository,
            ObjectStorageService objectStorageService
    ) {
        this.processingJobRepository = processingJobRepository;
        this.processingOutputRepository = processingOutputRepository;
        this.objectStorageService = objectStorageService;
    }

    @Override
    public List<JobListResponse> listJobs() {
        List<ProcessingJob> jobs = processingJobRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return jobs.stream()
                .map(job -> new JobListResponse(
                        job.getId(),
                        job.getVideo().getId(),
                        job.getVideo().getOriginalFilename(),
                        job.getVideo().getFileSize(),
                        job.getVideo().getContentType(),
                        job.getStatus(),
                        job.getProgress(),
                        job.getRetryCount(),
                        job.getCreatedAt(),
                        job.getStartedAt(),
                        job.getCompletedAt(),
                        job.getLastError()
                ))
                .toList();
    }

    @Override
    public JobDetailResponse getJobDetail(UUID jobId) {
        ProcessingJob job = processingJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Processing job not found: " + jobId
                ));

        List<ProcessingOutput> outputs = processingOutputRepository.findByJobId(jobId);

        List<OutputResponse> outputResponses = outputs.stream()
                .map(output -> {
                    String downloadUrl = null;
                    try {
                        // Build a descriptive filename for Content-Disposition: attachment
                        String baseName = job.getVideo().getOriginalFilename();
                        String nameWithoutExt = baseName != null && baseName.contains(".")
                                ? baseName.substring(0, baseName.lastIndexOf('.'))
                                : (baseName != null ? baseName : "video");
                        String suffix = output.getResolution() != null
                                ? output.getResolution()
                                : output.getType().name().toLowerCase();
                        String ext = output.getType().name().equals("THUMBNAIL") ? ".jpg" : ".mp4";
                        String downloadFilename = nameWithoutExt + "-" + suffix + ext;

                        downloadUrl = objectStorageService.generatePresignedDownloadUrl(
                                output.getS3Key(),
                                PRESIGNED_URL_EXPIRY_MINUTES,
                                downloadFilename
                        );
                    } catch (Exception ignored) {
                        // If presigned URL generation fails, return null � UI handles gracefully
                    }
                    return new OutputResponse(
                            output.getId(),
                            output.getType(),
                            output.getResolution(),
                            output.getFileSize(),
                            output.getContentType(),
                            downloadUrl,
                            output.getCreatedAt()
                    );
                })
                .toList();

        return new JobDetailResponse(
                job.getId(),
                job.getVideo().getId(),
                job.getVideo().getOriginalFilename(),
                job.getVideo().getFileSize(),
                job.getVideo().getContentType(),
                job.getStatus(),
                job.getProgress(),
                job.getRetryCount(),
                job.getMaxRetries(),
                job.getCreatedAt(),
                job.getStartedAt(),
                job.getCompletedAt(),
                job.getLastError(),
                job.getWidth(),
                job.getHeight(),
                job.getDuration(),
                outputResponses
        );
    }

    @Override
    public JobStatusResponse getJobStatus(UUID jobId) {
        ProcessingJob job = processingJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Processing job not found: " + jobId
                ));

        return new JobStatusResponse(
                job.getId(),
                job.getStatus(),
                job.getProgress(),
                job.getLastError()
        );
    }
    @Override
    public VideoQueryService.OutputStreamResult streamOutput(java.util.UUID jobId, java.util.UUID outputId) {
        com.videoprocessing.api.entity.ProcessingJob job = processingJobRepository.findById(jobId)
                .orElseThrow(() -> new com.videoprocessing.api.exception.ResourceNotFoundException(
                        "Processing job not found: " + jobId
                ));

        com.videoprocessing.api.entity.ProcessingOutput output = processingOutputRepository.findById(outputId)
                .orElseThrow(() -> new com.videoprocessing.api.exception.ResourceNotFoundException(
                        "Output not found: " + outputId
                ));

        // Build a meaningful filename for Content-Disposition
        String baseName = job.getVideo().getOriginalFilename();
        String nameWithoutExt = baseName != null && baseName.contains(".")
                ? baseName.substring(0, baseName.lastIndexOf('.'))
                : (baseName != null ? baseName : "video");
        String suffix = output.getResolution() != null
                ? output.getResolution()
                : output.getType().name().toLowerCase();
        String ext = output.getType().name().equals("THUMBNAIL") ? ".jpg" : ".mp4";
        String downloadFilename = nameWithoutExt + "-" + suffix + ext;

        String contentType = output.getContentType() != null
                ? output.getContentType()
                : (output.getType().name().equals("THUMBNAIL") ? "image/jpeg" : "video/mp4");

        long fileSize = output.getFileSize() != null ? output.getFileSize() : -1L;

        java.io.InputStream stream = objectStorageService.streamObject(output.getS3Key());

        return new VideoQueryService.OutputStreamResult(stream, downloadFilename, contentType, fileSize);
    }
}