package com.videoprocessing.api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "processing_outputs",
        indexes = {
                @Index(name = "idx_processing_outputs_job_id", columnList = "job_id")
        }
)
@Data
public class ProcessingOutput {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private ProcessingJob job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingOutputType type;

    private String resolution;

    @Column(nullable = false)
    private String s3Key;

    private Long fileSize;

    private String contentType;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public ProcessingOutput() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }


}