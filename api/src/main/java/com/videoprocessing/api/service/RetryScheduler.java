package com.videoprocessing.api.service;

import com.videoprocessing.api.entity.ProcessingJob;
import com.videoprocessing.api.entity.ProcessingJobStatus;
import com.videoprocessing.api.event.VideoProcessingEvent;
import com.videoprocessing.api.repository.ProcessingJobRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class RetryScheduler {

    private final ProcessingJobRepository processingJobRepository;
    private final VideoProcessingEventPublisher eventPublisher;

    public RetryScheduler(
            ProcessingJobRepository processingJobRepository,
            VideoProcessingEventPublisher eventPublisher
    ) {
        this.processingJobRepository = processingJobRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @Scheduled(fixedDelay = 30_000)
    public void publishReadyRetries() {

        List<ProcessingJob> jobs =
                processingJobRepository
                        .findByStatusAndNextRetryAtLessThanEqual(
                                ProcessingJobStatus.QUEUED,
                                Instant.now()
                        );

        for (ProcessingJob job : jobs) {

            job.setNextRetryAt(null);

            processingJobRepository.save(job);

            eventPublisher.publish(
                    new VideoProcessingEvent(
                            job.getId(),
                            job.getVideo().getId(),
                            job.getVideo().getOriginalS3Key()
                    )
            );
        }
    }
}