package com.videoprocessing.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration.
 *
 * <p>The allowed-origins default is {@code http://localhost:3000} for local
 * development only. In production, set the {@code APP_CORS_ALLOWED_ORIGINS}
 * environment variable to the exact origin(s) of your frontend.
 *
 * <p>Example (multiple origins, comma-separated):
 * <pre>APP_CORS_ALLOWED_ORIGINS=https://app.example.com,https://www.example.com</pre>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}