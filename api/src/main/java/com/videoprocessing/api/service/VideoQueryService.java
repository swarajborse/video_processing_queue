package com.videoprocessing.api.service;

import com.videoprocessing.api.dto.JobDetailResponse;
import com.videoprocessing.api.dto.JobListResponse;
import com.videoprocessing.api.dto.JobStatusResponse;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface VideoQueryService {

    List<JobListResponse> listJobs();

    JobDetailResponse getJobDetail(UUID jobId);

    JobStatusResponse getJobStatus(UUID jobId);

    /**
     * Returns a streaming InputStream for the given output file plus its resolved filename.
     * The array contains: [0] InputStream, [1] filename (String), [2] contentType (String)
     */
    OutputStreamResult streamOutput(UUID jobId, UUID outputId);

    record OutputStreamResult(InputStream stream, String filename, String contentType, long fileSize) {}
}
