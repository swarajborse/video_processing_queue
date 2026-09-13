package com.videoprocessing.api.service;

import com.videoprocessing.api.enum_.VideoResolution;
import com.videoprocessing.api.model.VideoMetadata;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ResolutionSelector {

    public List<VideoResolution> select(VideoMetadata metadata) {

        return Arrays.stream(VideoResolution.values())
                .filter(resolution ->
                        metadata.height() >= resolution.height())
                .toList();
    }
}