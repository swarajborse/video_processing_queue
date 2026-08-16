package com.videoprocessing.api.controller;

import com.videoprocessing.api.service.FFmpegService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/test/ffmpeg")
public class FFmpegTestController {

    private final FFmpegService ffmpegService;

    public FFmpegTestController(FFmpegService ffmpegService) {
        this.ffmpegService = ffmpegService;
    }

    @PostMapping
    public String testFFmpeg() {

        Path input = Path.of("traial.mp4.mp4");
        Path output = Path.of("processed-java.mp4");


        ffmpegService.execute(input, output);

        return "FFmpeg processing completed";
    }
}