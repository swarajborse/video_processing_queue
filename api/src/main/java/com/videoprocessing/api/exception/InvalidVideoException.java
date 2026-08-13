package com.videoprocessing.api.exception;

public class InvalidVideoException extends RuntimeException {
    public InvalidVideoException(String message) {
        super(message);
    }
}
