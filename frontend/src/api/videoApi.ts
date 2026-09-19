import { apiClient } from "./client";

// -- Types -------------------------------------------------------------------

export type JobStatus = "QUEUED" | "PROCESSING" | "COMPLETED" | "FAILED";
export type OutputType = "VIDEO" | "THUMBNAIL";

export interface UploadResponse {
  videoId: string;
  jobId: string;
  status: string;
}

export interface JobListItem {
  jobId: string;
  videoId: string;
  filename: string;
  fileSize: number | null;
  contentType: string | null;
  status: JobStatus;
  progress: number;
  retryCount: number;
  createdAt: string;
  startedAt: string | null;
  completedAt: string | null;
  lastError: string | null;
}

export interface OutputItem {
  outputId: string;
  type: OutputType;
  resolution: string | null;
  fileSize: number | null;
  contentType: string | null;
  downloadUrl: string | null;
  createdAt: string;
}

export interface JobDetail extends JobListItem {
  maxRetries: number;
  width: number | null;
  height: number | null;
  duration: number | null;
  outputs: OutputItem[];
}

export interface JobStatusPoll {
  jobId: string;
  status: JobStatus;
  progress: number;
  lastError: string | null;
}

interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
}

// -- API Functions ------------------------------------------------------------

export async function uploadVideo(
  file: File,
  idempotencyKey: string,
  onUploadProgress?: (pct: number) => void
): Promise<UploadResponse> {
  const formData = new FormData();
  formData.append("file", file);

  const res = await apiClient.post<ApiResponse<UploadResponse>>(
    "/api/videos/upload",
    formData,
    {
      headers: {
        "Idempotency-Key": idempotencyKey,
        "Content-Type": "multipart/form-data",
      },
      onUploadProgress: (event) => {
        if (event.total && onUploadProgress) {
          onUploadProgress(Math.round((event.loaded / event.total) * 100));
        }
      },
    }
  );
  return res.data.data;
}

export async function getJobs(): Promise<JobListItem[]> {
  const res = await apiClient.get<ApiResponse<JobListItem[]>>("/api/videos/jobs");
  return res.data.data;
}

export async function getJob(jobId: string): Promise<JobDetail> {
  const res = await apiClient.get<ApiResponse<JobDetail>>(
    `/api/videos/jobs/${jobId}`
  );
  return res.data.data;
}

export async function getJobStatus(jobId: string): Promise<JobStatusPoll> {
  const res = await apiClient.get<ApiResponse<JobStatusPoll>>(
    `/api/videos/jobs/${jobId}/status`
  );
  return res.data.data;
}
