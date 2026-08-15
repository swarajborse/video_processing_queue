package com.videoprocessing.api.service;

import java.nio.file.Path;

public record ProcessingWorkspace(
        Path directory,
        Path originalVideo
) {
}