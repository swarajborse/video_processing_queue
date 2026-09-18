package com.videoprocessing.api.dto;

public record ApiResponse<T>(
        boolean success,
        T data,
        String message
) {
}