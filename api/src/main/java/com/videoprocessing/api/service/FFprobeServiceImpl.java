package com.videoprocessing.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoprocessing.api.exception.VideoProcessingException;
import com.videoprocessing.api.model.VideoMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
public class FFprobeServiceImpl implements FFprobeService {

    private static final Logger log = LoggerFactory.getLogger(FFprobeServiceImpl.class);

    private final ObjectMapper objectMapper;
    private final String ffprobeExecutable;

    public FFprobeServiceImpl(
            ObjectMapper objectMapper,
            @Value("${ffprobe.executable:ffprobe}") String ffprobeExecutable
    ) {
        this.objectMapper = objectMapper;
        this.ffprobeExecutable = ffprobeExecutable;
    }

    @Override
    public VideoMetadata probe(Path input) {

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    ffprobeExecutable,
                    "-v", "error",
                    "-show_streams",
                    "-show_format",
                    "-of", "json",
                    input.toAbsolutePath().toString()
            );
            // Separate stderr so we only parse stdout (JSON output)
            processBuilder.redirectErrorStream(false);

            Process process = processBuilder.start();

            // Read stdout (JSON) and stderr concurrently to prevent deadlock on large output
            String output = new String(process.getInputStream().readAllBytes());

            // Drain stderr to debug log — keeps filesystem paths out of exception messages
            Thread stderrThread = new Thread(() -> {
                try {
                    byte[] stderrBytes = process.getErrorStream().readAllBytes();
                    if (stderrBytes.length > 0) {
                        log.debug("[ffprobe stderr] {}", new String(stderrBytes));
                    }
                } catch (IOException ignored) {
                }
            }, "ffprobe-stderr-drainer");
            stderrThread.setDaemon(true);
            stderrThread.start();

            boolean finished = process.waitFor(5, TimeUnit.MINUTES);
            stderrThread.join();

            if (!finished) {
                process.destroyForcibly();
                throw new VideoProcessingException("FFprobe timed out", null);
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new VideoProcessingException("FFprobe failed with exit code " + exitCode, null);
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
                throw new VideoProcessingException("No video stream found in uploaded file", null);
            }

            int width = videoStream.get("width").asInt();
            int height = videoStream.get("height").asInt();
            double duration = root.get("format").get("duration").asDouble();

            return new VideoMetadata(width, height, duration);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VideoProcessingException("FFprobe was interrupted", e);
        } catch (IOException e) {
            throw new VideoProcessingException("Unable to execute FFprobe", e);
        }
    }
}