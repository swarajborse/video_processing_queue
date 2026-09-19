package com.videoprocessing.api.repository;

import com.videoprocessing.api.entity.ProcessingOutput;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcessingOutputRepository
        extends JpaRepository<ProcessingOutput, UUID> {

    List<ProcessingOutput> findByJobId(UUID jobId);
}
