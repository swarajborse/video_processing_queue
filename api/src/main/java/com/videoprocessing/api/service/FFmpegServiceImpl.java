package com.videoprocessing.api.service;

import com.videoprocessing.api.enum_.VideoResolution;
import com.videoprocessing.api.exception.VideoProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
public class FFmpegServiceImpl implements FFmpegService {

    private static final Logger log = LoggerFactory.getLogger(FFmpegServiceImpl.class);

    private final String ffmpegExecutable;

    public FFmpegServiceImpl(
            @Value("${ffmpeg.executable:ffmpeg}") String ffmpegExecutable
    ) {
        this.ffmpegExecutable = ffmpegExecutable;
    }

    @Override
    public void execute(
            Path input,
            Path output,
            VideoResolution resolution
    ) {
        ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegExecutable,
                "-y",
                "-i", input.toAbsolutePath().toString(),
                "-vf", "scale=-2:" + resolution.height(),
                "-c:v", "libx264",
                "-preset", "fast",
                "-crf", "23",
                "-c:a", "aac",
                output.toAbsolutePath().toString()
        );
        // Redirect stderr into stdout so we can capture all output in one stream
        processBuilder.redirectErrorStream(true);

        runProcess(processBuilder, "encode to " + resolution.height() + "p", output);
    }

    @Override
    public void generateThumbnail(Path input, Path output) {
        ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegExecutable,
                "-y",
                "-ss", "00:00:01",
                "-i", input.toAbsolutePath().toString(),
                "-frames:v", "1",
                output.toAbsolutePath().toString()
        );
        processBuilder.redirectErrorStream(true);

        runProcess(processBuilder, "thumbnail", output);
    }

    /**
     * Shared process-execution logic: start the process, drain its output to the
     * SLF4J logger (not System.out), wait up to 30 minutes, and validate the result.
     *
     * <p>Paths are intentionally not included in exception messages that propagate
     * up to the GlobalExceptionHandler, preventing filesystem details from leaking
     * into API responses.
     */
    private void runProcess(ProcessBuilder processBuilder, String operation, Path expectedOutput) {
        try {
            Process process = processBuilder.start();

            // Drain FFmpeg output to logger (debug level) — keeps it out of System.out
            // and away from log aggregators that might forward to clients.
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader =
                             new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.debug("[ffmpeg] {}", line);
                    }
                } catch (IOException ignored) {
                    // Process closed — nothing to do
                }
            }, "ffmpeg-output-drainer");
            outputThread.setDaemon(true);
            outputThread.start();

            boolean finished = process.waitFor(30, TimeUnit.MINUTES);

            if (!finished) {
                process.destroyForcibly();
                throw new VideoProcessingException(
                        "FFmpeg timed out during " + operation,
                        null
                );
            }

            outputThread.join();

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new VideoProcessingException(
                        "FFmpeg failed (exit code " + exitCode + ") during " + operation,
                        null
                );
            }

            if (!Files.exists(expectedOutput)) {
                throw new VideoProcessingException(
                        "FFmpeg completed but output file was not created during " + operation,
                        null
                );
            }

            if (Files.size(expectedOutput) == 0) {
                throw new VideoProcessingException(
                        "FFmpeg created an empty output file during " + operation,
                        null
                );
            }

        } catch (IOException e) {
            throw new VideoProcessingException("Failed to start FFmpeg for " + operation, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VideoProcessingException("FFmpeg was interrupted during " + operation, e);
        }
    }
}