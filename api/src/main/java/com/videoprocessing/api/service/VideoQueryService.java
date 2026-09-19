package com.videoprocessing.api.service;

import com.videoprocessing.api.dto.JobDetailResponse;
import com.videoprocessing.api.dto.JobListResponse;
import com.videoprocessing.api.dto.JobStatusResponse;

import java.util.List;
import java.util.UUID;

public interface VideoQueryService {

    List<JobListResponse> listJobs();

    JobDetailResponse getJobDetail(UUID jobId);

    JobStatusResponse getJobStatus(UUID jobId);
}
