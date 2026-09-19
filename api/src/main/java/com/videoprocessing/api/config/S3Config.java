package com.videoprocessing.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(
            @Value("${aws.s3.region}") String region,
            @Value("${aws.s3.endpoint}") String endpoint,
            @Value("${aws.access-key:}") String accessKey,
            @Value("${aws.secret-key:}") String secretKey
    ) {
        var builder = S3Client.builder()
                .region(Region.of(region));

        if (!endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint))
                    .forcePathStyle(true);
        }

        if (!accessKey.isBlank() && !secretKey.isBlank()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)
                    )
            );
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(
            @Value("${aws.s3.region}") String region,
            @Value("${aws.s3.endpoint}") String endpoint,
            @Value("${aws.s3.presigned-url-endpoint:}") String presignedUrlEndpoint,
            @Value("${aws.access-key:}") String accessKey,
            @Value("${aws.secret-key:}") String secretKey
    ) {
        var builder = S3Presigner.builder()
                .region(Region.of(region));

        // Use a separate public endpoint for presigned URLs when running behind Docker,
        // so that generated URLs resolve to localhost instead of the internal service name.
        String effectiveEndpoint = !presignedUrlEndpoint.isBlank() ? presignedUrlEndpoint : endpoint;

        if (!effectiveEndpoint.isBlank()) {
            builder.endpointOverride(URI.create(effectiveEndpoint))
                    .serviceConfiguration(
                            S3Configuration.builder()
                                    .pathStyleAccessEnabled(true)
                                    .build()
                    );
        }

        if (!accessKey.isBlank() && !secretKey.isBlank()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)
                    )
            );
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
}