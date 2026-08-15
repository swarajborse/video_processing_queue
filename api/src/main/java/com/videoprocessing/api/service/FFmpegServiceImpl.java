package com.videoprocessing.api.service;

import com.videoprocessing.api.exception.VideoProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class FFmpegServiceImpl implements FFmpegService {

    @Value("${ffmpeg.executable}")
    private String ffmpegExecutable;


    @Override
    public void execute(
            Path input,
            Path output
    ) {

        ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegExecutable,
                "-y",
                "-i",
                input.toString(),
                output.toString()
        );
        processBuilder.redirectErrorStream(true);

        try {

            Process process =
                    processBuilder.start();

            int exitCode =
                    process.waitFor();

            if (exitCode != 0) {

                throw new VideoProcessingException(
                        "FFmpeg processing failed",
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