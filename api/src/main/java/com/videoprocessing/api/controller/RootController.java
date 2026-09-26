package com.videoprocessing.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "VidFlow Video Processing API",
                "version", "1.0",
                "docs", "/swagger-ui/index.html",
                "health", "/actuator/health",
                "api", "/api/videos"
        ));
    }
}
