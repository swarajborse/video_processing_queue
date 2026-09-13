package com.videoprocessing.api.enum_;

public enum VideoResolution {

    P1080(1080),
    P720(720),
    P480(480);

    private final int height;

    VideoResolution(int height) {
        this.height = height;
    }

    public int height() {
        return height;
    }
}
