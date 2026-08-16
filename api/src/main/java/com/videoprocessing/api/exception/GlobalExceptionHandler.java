package com.videoprocessing.api.exception;

import com.videoprocessing.api.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex
    ) {

        ex.printStackTrace();

        ErrorResponse response = new ErrorResponse(
                500,
                "INTERNAL_SERVER_ERROR",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(InvalidVideoException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVideo(
            InvalidVideoException exception
    ) {

        ErrorResponse response =
                new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "BAD_REQUEST",
                        exception.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(ObjectStorageException.class)
    public ResponseEntity<ErrorResponse> handleObjectStorageException(
            ObjectStorageException exception
    ){
        ErrorResponse response =new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                exception.getMessage()
        );
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }




    @ExceptionHandler(VideoProcessingException.class)
    public ResponseEntity<Map<String, Object>> handleVideoProcessingException(
            VideoProcessingException e
    ) {

        Map<String, Object> response = Map.of(
                "status", 500,
                "message", "VIDEO_PROCESSING_ERROR",
                "error", e.getMessage(),
                "timestamp", Instant.now().toString()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}