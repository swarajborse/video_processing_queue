package com.videoprocessing.api.service;

import com.videoprocessing.api.event.VideoProcessingEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeadLetterEventPublisher {

    private final KafkaTemplate<String, VideoProcessingEvent> kafkaTemplate;
    private final String dlqTopic;

    public DeadLetterEventPublisher(
            KafkaTemplate<String, VideoProcessingEvent> kafkaTemplate,
            @Value("${video.processing.kafka.dlq-topic}")
            String dlqTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.dlqTopic = dlqTopic;
    }

    public void publish(VideoProcessingEvent event) {
        kafkaTemplate.send(
                dlqTopic,
                event.jobId().toString(),
                event
        );
    }
}