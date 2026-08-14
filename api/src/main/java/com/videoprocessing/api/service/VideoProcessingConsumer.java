package com.videoprocessing.api.service;

import com.videoprocessing.api.event.VideoProcessingEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class VideoProcessingConsumer {

    private final VideoProcessingService processingService;

    public VideoProcessingConsumer(
            VideoProcessingService processingService
    ) {
        this.processingService = processingService;
    }

    @KafkaListener(
            topics = "${app.kafka.video-processing-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(VideoProcessingEvent event) {

        processingService.process(event);
    }
}