package com.videoprocessing.api.repository;

import com.videoprocessing.api.entity.ProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, UUID> {
}