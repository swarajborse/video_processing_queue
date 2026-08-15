package com.videoprocessing.api.service;

import com.videoprocessing.api.event.VideoProcessingEvent;
import org.springframework.stereotype.Service;

@Service
public class VideoProcessingServiceImpl
        implements VideoProcessingService {

    private final VideoProcessingWorker worker;

    public VideoProcessingServiceImpl(
            VideoProcessingWorker worker
    ) {
        this.worker = worker;
    }

    @Override
    public void process(VideoProcessingEvent event) {

        worker.process(event);
    }
}