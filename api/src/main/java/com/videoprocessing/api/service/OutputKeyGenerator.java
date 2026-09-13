package com.videoprocessing.api.service;

import com.videoprocessing.api.enum_.VideoResolution;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OutputKeyGenerator {

    public String outputKey(
            UUID videoId,
            VideoResolution resolution
    ) {
        return "videos/"
                + videoId
                + "/outputs/"
                + resolution.height()
                + "p/video-"
                + resolution.height()
                + "p.mp4";
    }

    public String thumbnailKey(UUID videoId) {
        return "videos/"
                + videoId
                + "/outputs/thumbnail/thumbnail.jpg";
    }
}