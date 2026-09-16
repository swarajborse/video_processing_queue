package com.videoprocessing.api.repository;

import com.videoprocessing.api.entity.ProcessingJob;
import com.videoprocessing.api.entity.ProcessingJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessingJobRepository
        extends JpaRepository<ProcessingJob, UUID> {

    Optional<ProcessingJob> findById(UUID id);


    List<ProcessingJob>
    findByStatusAndLastHeartbeatAtBefore(
            ProcessingJobStatus status,
            Instant time
    );

    List<ProcessingJob> findByStatusAndNextRetryAtLessThanEqual(
            ProcessingJobStatus status,
            Instant time
    );

}