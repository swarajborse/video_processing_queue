import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { Header } from "../components/Header";
import { StatusBadge } from "../components/StatusBadge";
import { LoadingSpinner } from "../components/LoadingSpinner";
import { useJobStatus } from "../hooks/useJobStatus";
import { getJob, type JobDetail, type OutputItem } from "../api/videoApi";
import { extractErrorMessage } from "../api/client";
import { formatDate } from "../utils/formatDate";
import { formatDuration, formatVideoDuration } from "../utils/formatDuration";
import { formatFileSize } from "../utils/formatFileSize";

function InfoRow({ label, value, mono }: { label: string; value: React.ReactNode; mono?: boolean }) {
  if (value === null || value === undefined || value === "\u2014") return null;
  return (
    <div className="flex items-baseline gap-4 py-1.5">
      <span className="text-xs text-slate-500 w-28 shrink-0">{label}</span>
      <span className={`text-sm text-slate-800 ${mono ? "mono" : ""}`}>{value}</span>
    </div>
  );
}

function OutputRow({ output, jobId }: { output: OutputItem; jobId: string }) {
  const label = output.type === "THUMBNAIL"
    ? "Thumbnail"
    : output.resolution ?? output.type;

  // Route all downloads through the API proxy (/api/videos/jobs/{jobId}/outputs/{outputId}/download)
  // This avoids direct browser→MinIO connections which may be blocked or time out.
  const proxyDownloadUrl = `/api/videos/jobs/${jobId}/outputs/${output.outputId}/download`;

  return (
    <div className="flex items-center justify-between py-2 border-b border-slate-100 last:border-0">
      <div className="flex items-center gap-3">
        <span className="text-sm font-medium text-slate-800">{label}</span>
        {output.fileSize != null && (
          <span className="text-xs text-slate-400">{formatFileSize(output.fileSize)}</span>
        )}
      </div>
      <a
        id={`download-${output.outputId}`}
        href={proxyDownloadUrl}
        className="btn-secondary text-xs"
        download
      >
        Download
      </a>
    </div>
  );
}

export const JobDetails: React.FC = () => {
  const { jobId } = useParams<{ jobId: string }>();
  const [detail, setDetail] = useState<JobDetail | null>(null);
  const [detailLoading, setDetailLoading] = useState(true);
  const [detailError, setDetailError] = useState<string | null>(null);

  const { status: liveStatus } = useJobStatus(jobId);

  useEffect(() => {
    if (!jobId) return;
    setDetailLoading(true);
    getJob(jobId)
      .then(setDetail)
      .catch((err) => setDetailError(extractErrorMessage(err)))
      .finally(() => setDetailLoading(false));
  }, [jobId]);

  useEffect(() => {
    if (!jobId) return;
    if (liveStatus?.status === "COMPLETED" || liveStatus?.status === "FAILED") {
      getJob(jobId).then(setDetail).catch(() => {});
    }
  }, [liveStatus?.status, jobId]);

  const effectiveStatus = liveStatus?.status ?? detail?.status;

  return (
    <div className="flex-1 overflow-y-auto">
      <Header title="Job Details" />

      <div className="p-6 space-y-4 max-w-3xl">
        <Link to="/jobs" className="text-sm text-blue-600 hover:underline">
          &larr; Back to Jobs
        </Link>

        {detailLoading && (
          <div className="card px-4 py-8 flex justify-center">
            <LoadingSpinner text="Loading job details..." />
          </div>
        )}

        {detailError && (
          <div className="card border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {detailError}
          </div>
        )}

        {detail && (
          <>
            {/* Header: filename + status */}
            <div className="flex items-center gap-3">
              <h2 className="text-lg font-semibold text-slate-900 truncate">{detail.filename}</h2>
              <StatusBadge status={effectiveStatus ?? detail.status} />
            </div>

            {/* Status context */}
            {effectiveStatus === "PROCESSING" && (
              <div className="card px-4 py-2.5 flex items-center gap-2 text-sm text-slate-600">
                <LoadingSpinner size="sm" />
                <span>Worker is processing the video...</span>
              </div>
            )}
            {effectiveStatus === "QUEUED" && (
              <div className="card px-4 py-2.5 text-sm text-slate-600">
                Waiting for worker...
              </div>
            )}
            {effectiveStatus === "COMPLETED" && (
              <div className="card px-4 py-2.5 text-sm text-green-700">
                Processing completed.
              </div>
            )}
            {effectiveStatus === "FAILED" && (
              <div className="card px-4 py-2.5 text-sm text-red-600">
                Processing failed.
              </div>
            )}

            {/* Job Info - compact grid */}
            <div className="card">
              <div className="px-4 py-3 border-b border-slate-200">
                <h2 className="section-title">Job Information</h2>
              </div>
              <div className="px-4 py-1">
                <InfoRow label="Job ID" value={<span className="mono">{detail.jobId}</span>} />
                <InfoRow label="Status" value={<StatusBadge status={effectiveStatus ?? detail.status} />} />
                <InfoRow
                  label="Duration"
                  value={formatDuration(detail.startedAt, detail.completedAt)}
                />
                <InfoRow label="Created" value={formatDate(detail.createdAt)} />
                <InfoRow
                  label="Started"
                  value={detail.startedAt ? formatDate(detail.startedAt) : "\u2014"}
                />
                <InfoRow
                  label="Completed"
                  value={detail.completedAt ? formatDate(detail.completedAt) : "\u2014"}
                />
                <InfoRow label="File Size" value={formatFileSize(detail.fileSize)} />
                <InfoRow label="Retries" value={`${detail.retryCount} / ${detail.maxRetries}`} />
              </div>
            </div>

            {/* Error details */}
            {(effectiveStatus === "FAILED") && (liveStatus?.lastError ?? detail.lastError) && (
              <div className="card border-red-200">
                <div className="px-4 py-2.5 border-b border-red-100 bg-red-50">
                  <h2 className="section-title text-red-800 text-sm">Error Details</h2>
                </div>
                <div className="px-4 py-3">
                  <pre className="text-xs text-red-700 whitespace-pre-wrap break-words font-mono">
                    {liveStatus?.lastError ?? detail.lastError}
                  </pre>
                </div>
              </div>
            )}

            {/* Video Metadata */}
            {(detail.width != null || detail.height != null || detail.duration != null) && (
              <div className="card">
                <div className="px-4 py-3 border-b border-slate-200">
                  <h2 className="section-title">Video Metadata</h2>
                </div>
                <div className="px-4 py-1">
                  {detail.width != null && detail.height != null && (
                    <InfoRow
                      label="Resolution"
                      value={`${detail.width} \u00d7 ${detail.height}`}
                    />
                  )}
                  <InfoRow label="Duration" value={formatVideoDuration(detail.duration)} />
                  <InfoRow label="File Size" value={formatFileSize(detail.fileSize)} />
                </div>
              </div>
            )}

            {/* Outputs */}
            {effectiveStatus === "COMPLETED" && detail.outputs.length > 0 && (
              <div className="card">
                <div className="px-4 py-3 border-b border-slate-200">
                  <h2 className="section-title">Outputs</h2>
                </div>
                <div className="px-4 py-1">
                  {detail.outputs.map((output) => (
                    <OutputRow key={output.outputId} output={output} jobId={detail.jobId} />
                  ))}
                </div>
              </div>
            )}

            {effectiveStatus === "COMPLETED" && detail.outputs.length === 0 && (
              <div className="card px-4 py-3 text-sm text-slate-500">
                Processing completed but no output files were recorded.
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};
