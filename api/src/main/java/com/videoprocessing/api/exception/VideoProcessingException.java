package com.videoprocessing.api.exception;

public class VideoProcessingException extends RuntimeException {
    public VideoProcessingException(String message,Throwable cause) {
        super(message,cause);
    }
}
