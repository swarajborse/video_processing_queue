package com.videoprocessing.api.service;

import com.videoprocessing.api.entity.ProcessingJob;
import com.videoprocessing.api.entity.ProcessingJobStatus;
import com.videoprocessing.api.repository.ProcessingJobRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class StuckJobRecoveryService {

    private final ProcessingJobRepository processingJobRepository;

    public StuckJobRecoveryService(
            ProcessingJobRepository processingJobRepository
    ) {
        this.processingJobRepository =
                processingJobRepository;
    }

    @Scheduled(fixedDelay = 60_000)
    public void recoverStuckJobs() {

        Instant threshold =
                Instant.now().minus(Duration.ofMinutes(30));

        List<ProcessingJob> stuckJobs =
                processingJobRepository
                        .findByStatusAndLastHeartbeatAtBefore(
                                ProcessingJobStatus.PROCESSING,
                                threshold
                        );

        for (ProcessingJob job : stuckJobs) {

            job.setStatus(
                    ProcessingJobStatus.FAILED
            );

            job.setLastError(
                    "Worker heartbeat timed out"
            );

            processingJobRepository.save(job);
        }
    }
}