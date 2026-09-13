package com.videoprocessing.api.service;

import com.videoprocessing.api.enum_.VideoResolution;

import java.nio.file.Path;

public record ProcessingWorkspace(
        Path directory,
        Path originalVideo
) {
    public Path outputPath(VideoResolution resolution) {

        return switch (resolution) {
            case P1080 ->
                    directory.resolve("video-1080p.mp4");

            case P720 ->
                    directory.resolve("video-720p.mp4");

            case P480 ->
                    directory.resolve("video-480p.mp4");
        };
    }

    public Path thumbnailPath() {
        return directory.resolve("thumbnail.jpg");
    }
}