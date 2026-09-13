package com.videoprocessing.api.service;

import com.videoprocessing.api.enum_.VideoResolution;

import java.nio.file.Path;

public interface FFmpegService {

    void execute(
            Path input,
            Path output,
            VideoResolution resolution
    );

    void generateThumbnail(
            Path input,
            Path output
    );
}
