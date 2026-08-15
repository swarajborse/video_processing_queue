package com.videoprocessing.api.service;

import com.videoprocessing.api.exception.ObjectStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class S3ObjectStorageService implements ObjectStorageService {

    private final S3Client s3Client;
    private final String bucket;

    public S3ObjectStorageService(
            S3Client s3Client,
            @Value("${aws.s3.bucket}") String bucket
    ) {
        this.s3Client = s3Client;
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
}