package com.videoprocessing.api.service;

import com.videoprocessing.api.event.VideoProcessingEvent;

public interface VideoProcessingEventPublisher {
    void publish(VideoProcessingEvent event);

}
