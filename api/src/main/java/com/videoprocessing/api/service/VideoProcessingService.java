package com.videoprocessing.api.service;

import com.videoprocessing.api.entity.Video;
import com.videoprocessing.api.event.VideoProcessingEvent;

public interface VideoProcessingService {
    void process(VideoProcessingEvent event);
}
