package com.videoprocessing.api.service;

import com.videoprocessing.api.event.VideoProcessingEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaVideoProcessingEventPublisher
        implements VideoProcessingEventPublisher {

    private final KafkaTemplate<String, VideoProcessingEvent> kafkaTemplate;
    private final String topic;

    public KafkaVideoProcessingEventPublisher(
            KafkaTemplate<String, VideoProcessingEvent> kafkaTemplate,
            @Value("${app.kafka.video-processing-topic}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(VideoProcessingEvent event) {

        kafkaTemplate.send(
                topic,
                event.videoId().toString(),
                event
        );
    }
}