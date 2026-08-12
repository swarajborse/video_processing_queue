package com.videoprocessing.api.dto;

import com.videoprocessing.api.entity.VideoStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class VideoUploadResponse {

    private UUID videoId;
    private UUID jobId;
    private VideoStatus status;
}