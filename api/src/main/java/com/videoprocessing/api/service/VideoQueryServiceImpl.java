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
                        downloadUrl = objectStorageService.generatePresignedDownloadUrl(
                                output.getS3Key(),
                                PRESIGNED_URL_EXPIRY_MINUTES
                        );
                    } catch (Exception ignored) {
                        // If presigned URL generation fails, return null — UI handles gracefully
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
}
