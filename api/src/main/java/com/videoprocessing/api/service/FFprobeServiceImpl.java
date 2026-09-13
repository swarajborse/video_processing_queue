package com.videoprocessing.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoprocessing.api.exception.VideoProcessingException;
import com.videoprocessing.api.model.VideoMetadata;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
@Service
public class FFprobeServiceImpl implements FFprobeService {

    private final ObjectMapper objectMapper;

    public FFprobeServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public VideoMetadata probe(Path input) {

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-show_streams",
                    "-show_format",
                    "-of", "json",
                    input.toString()
            );

            Process process = processBuilder.start();

            String output = new String(
                    process.getInputStream().readAllBytes()
            );

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new VideoProcessingException(
                        "FFprobe failed",
                        null
                );
            }

            JsonNode root = objectMapper.readTree(output);

            JsonNode videoStream = null;

            for (JsonNode stream : root.get("streams")) {
                if ("video".equals(stream.get("codec_type").asText())) {
                    videoStream = stream;
                    break;
                }
            }

            if (videoStream == null) {
                throw new VideoProcessingException(
                        "No video stream found",
                        null
                );
            }

            int width = videoStream.get("width").asInt();
            int height = videoStream.get("height").asInt();

            double duration =
                    root.get("format").get("duration").asDouble();

            return new VideoMetadata(
                    width,
                    height,
                    duration
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new VideoProcessingException(
                    "FFprobe interrupted",
                    e
            );

        } catch (IOException e) {

            throw new VideoProcessingException(
                    "Unable to execute FFprobe",
                    e
            );
        }
    }
}