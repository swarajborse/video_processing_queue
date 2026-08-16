
# Advanced Video Processing Queue

A production-oriented **asynchronous video processing backend** built with Spring Boot, Kafka, PostgreSQL, S3-compatible storage, and FFmpeg.

The system accepts video uploads through a REST API and processes them asynchronously using Kafka-based workers, keeping heavy video processing outside the API request lifecycle.

## Architecture

```text
Client
  ↓
Spring Boot REST API
  ↓
S3 ─────────── PostgreSQL
  ↓
Kafka
  ↓
Kafka Consumer
  ↓
Video Processing Worker
  ↓
S3 Download
  ↓
FFmpeg
  ↓
Processed Output
```

## Key Features

* **Asynchronous processing** using Apache Kafka and background workers
* **S3 object storage** for large video files
* **PostgreSQL** for durable video and processing-job state
* **Kafka event-driven architecture** with 3 partitions and consumer groups
* **Isolated processing workspaces** for each video-processing job
* **FFmpeg integration** through Java `ProcessBuilder`
* **FFmpeg safety controls** including 30-minute timeout, exit-code validation, output validation, and interruption handling
* **4-state job lifecycle:** `QUEUED → PROCESSING → COMPLETED / FAILED`
* Failure handling with persistent error information and workspace cleanup
* Designed for future **parallel workers, retries, DLQ, idempotency, crash recovery, and monitoring**

## Processing Flow

```text
POST /api/videos/upload
        ↓
Validate Video
        ↓
Upload Original → S3
        ↓
Create Job → PostgreSQL
        ↓
Publish Event → Kafka
        ↓
Kafka Consumer
        ↓
Video Processing Worker
        ↓
Download Original → Local Workspace
        ↓
FFmpeg Processing
        ↓
Validate Output
        ↓
Cleanup Workspace
```

Kafka messages contain only job metadata:

```java
public record VideoProcessingEvent(
        UUID jobId,
        UUID videoId,
        String originalS3Key
) {}
```

Video bytes are kept in S3 rather than transferred through Kafka.

## Testing & Verification

The system was verified across the main processing pipeline:

* REST API → FFmpeg integration testing
* Java `ProcessBuilder` → FFmpeg execution
* Kafka producer → consumer → worker flow
* S3 video download
* PostgreSQL processing-job state updates
* FFmpeg output validation
* Failure handling and temporary workspace cleanup
* FFmpeg processing benchmark of approximately **0.23 seconds** for the test video

## Project Metrics

| Metric                          |         Value |
| ------------------------------- | ------------: |
| Kafka partitions                |         **3** |
| Job states                      |         **4** |
| FFmpeg timeout                  |    **30 min** |
| Measured FFmpeg test processing | **~0.23 sec** |

## Tech Stack

**Java 21 · Spring Boot · Spring Data JPA · Hibernate · PostgreSQL · Apache Kafka · S3 · FFmpeg · FFprobe · Docker · Docker Compose · Maven · JUnit 5 · Mockito · Testcontainers**

## Engineering Highlights

* Decoupled API request handling from CPU-intensive video processing using Kafka.
* Kept large binary data in S3 while passing lightweight metadata through Kafka.
* Avoided long-running database transactions around FFmpeg execution.
* Used per-job temporary workspaces to prevent file collisions.
* Added process timeout and output validation around external FFmpeg execution.
* Designed the architecture to support horizontal worker scaling through Kafka partitions and consumer groups.

## Future Enhancements

* 1080p / 720p / 480p transcoding
* Thumbnail generation
* FFprobe metadata extraction
* Output upload and `ProcessingOutput` persistence
* Retry and Dead Letter Queue
* Idempotency and crash recovery
* Parallel workers and resource limits
* Prometheus/Grafana monitoring
* Integration and failure-test expansion
