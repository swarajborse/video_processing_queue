package com.videoprocessing.api.service;

import com.videoprocessing.api.exception.VideoProcessingException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Component
public class ProcessingWorkspaceManager {

    public ProcessingWorkspace create() {

        try {
            Path directory =
                    Files.createTempDirectory(
                            "video-processing-"
                    );

            Path originalVideo =
                    directory.resolve("original.mp4");

            return new ProcessingWorkspace(
                    directory,
                    originalVideo
            );

        } catch (IOException e) {

            throw new VideoProcessingException(
                    "Failed to create processing workspace",
                    e
            );
        }
    }

    public void cleanup(
            ProcessingWorkspace workspace
    ) {

        try {

            Files.walk(workspace.directory())
                    .sorted(
                            Comparator.reverseOrder()
                    )
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

        } catch (IOException e) {

            throw new VideoProcessingException(
                    "Failed to cleanup processing workspace",
                    e
            );
        }
    }
}