package com.videoprocessing.api.service;

import com.videoprocessing.api.exception.VideoProcessingException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
public class FFmpegServiceImpl implements FFmpegService {

    @Override
    public void execute(
            Path input,
            Path output
    ) {

        ProcessBuilder processBuilder =
                new ProcessBuilder(
                        "ffmpeg",
                        "-y",
                        "-i",
                        input.toString(),
                        output.toString()
                );

        processBuilder.redirectErrorStream(true);

        try {

            Process process =
                    processBuilder.start();

            Thread outputThread =
                    new Thread(() -> {
                        try {
                            process.getInputStream()
                                    .transferTo(System.out);
                        } catch (IOException ignored) {
                        }
                    });

            outputThread.start();

            boolean finished =
                    process.waitFor(
                            30,
                            TimeUnit.MINUTES
                    );

            if (!finished) {

                process.destroyForcibly();

                throw new VideoProcessingException(
                        "FFmpeg processing timed out",
                        null
                );
            }

            outputThread.join();

            int exitCode =
                    process.exitValue();

            if (exitCode != 0) {

                throw new VideoProcessingException(
                        "FFmpeg processing failed with exit code: "
                                + exitCode,
                        null
                );
            }

            if (!Files.exists(output)) {

                throw new VideoProcessingException(
                        "FFmpeg completed but output file was not created",
                        null
                );
            }

            if (Files.size(output) == 0) {

                throw new VideoProcessingException(
                        "FFmpeg created an empty output file",
                        null
                );
            }

        } catch (IOException e) {

            throw new VideoProcessingException(
                    "Failed to start FFmpeg",
                    e
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new VideoProcessingException(
                    "FFmpeg process was interrupted",
                    e
            );
        }
    }
}