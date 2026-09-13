package com.videoprocessing.api.service;

import com.videoprocessing.api.model.VideoMetadata;

import java.nio.file.Path;

public interface FFprobeService {

    VideoMetadata probe(Path input);
}
