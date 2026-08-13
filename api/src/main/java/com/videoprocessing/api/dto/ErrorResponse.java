package com.videoprocessing.api.dto;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ErrorResponse {

   private int status;
   private String message;
   private String error;
   private Instant  timestamp;

    public ErrorResponse(int status, String message, String error) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = Instant.now();
    }

}
