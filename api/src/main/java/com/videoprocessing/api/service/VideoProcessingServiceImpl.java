package com.videoprocessing.api.service;

import com.videoprocessing.api.event.VideoProcessingEvent;
import org.springframework.stereotype.Service;

@Service
public class VideoProcessingServiceImpl
        implements VideoProcessingService {

    @Override
    public void process(VideoProcessingEvent event) {

        System.out.println(
                "Processing video: " + event.videoId()
        );

        System.out.println(
                "Job ID: " + event.jobId()
        );

        System.out.println(
                "Original S3 key: " + event.originalS3Key()
        );
    }
}