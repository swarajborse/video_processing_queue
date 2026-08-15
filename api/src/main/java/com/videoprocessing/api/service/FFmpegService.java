package com.videoprocessing.api.service;

import java.nio.file.Path;

public interface FFmpegService {

    void execute(
            Path input,
            Path output
    );
}
