package com.videoprocessing.api.service;

import com.videoprocessing.api.exception.ObjectStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Service
public class S3ObjectStorageService implements ObjectStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    public S3ObjectStorageService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${aws.s3.bucket}") String bucket
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
    }

    @Override
    public String upload(
            MultipartFile file,
            String objectKey
    ) {
        try {
            PutObjectRequest request =
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .contentType(file.getContentType())
                            .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

            return objectKey;

        } catch (IOException e) {
            throw new ObjectStorageException(
                    "Failed to upload video to object storage",
                    e
            );
        }
    }

    @Override
    public void upload(Path file, String key) {
        try {
            if (!Files.exists(file)) {
                throw new IllegalArgumentException(
                        "File does not exist: " + file
                );
            }

            if (!Files.isRegularFile(file)) {
                throw new IllegalArgumentException(
                        "Path is not a regular file: " + file
                );
            }

            long fileSize = Files.size(file);
            String contentType = Files.probeContentType(file);

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            try (InputStream inputStream = Files.newInputStream(file)) {
                s3Client.putObject(
                        request,
                        RequestBody.fromInputStream(
                                inputStream,
                                fileSize
                        )
                );
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to upload local file to object storage: " + file,
                    e
            );
        }
    }

    @Override
    public void download(
            String objectKey,
            Path destination
    ) {
        try {
            GetObjectRequest request =
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .build();

            s3Client.getObject(
                    request,
                    destination
            );

        } catch (Exception e) {
            throw new ObjectStorageException(
                    "Failed to download video from object storage",
                    e
            );
        }
    }

    @Override
    public String generatePresignedDownloadUrl(String objectKey, int expirationMinutes, String filename) {
        try {
            // Build a sanitised Content-Disposition header value.
            // Using RFC 5987 ASCII fallback keeps it simple and widely compatible.
            String safeFilename = filename != null
                    ? filename.replaceAll("[^\\w.\\-]", "_")
                    : "download";

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .responseContentDisposition("attachment; filename=\"" + safeFilename + "\"")
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
            return presigned.url().toString();

        } catch (Exception e) {
            throw new ObjectStorageException(
                    "Failed to generate presigned URL for key: " + objectKey,
                    e
            );
        }
    }

    @Override
    public java.io.InputStream streamObject(String objectKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
            return response;
        } catch (Exception e) {
            throw new ObjectStorageException(
                    "Failed to stream object from storage: " + objectKey, e
            );
        }
    }
}
