package com.videoprocessing.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI videoProcessingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Video Processing Queue API")
                        .version("1.0")
                        .description(
                                "Asynchronous video processing API " +
                                        "using Kafka, FFmpeg and object storage."
                        )
                );
    }
}