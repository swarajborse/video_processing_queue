import React, { useState } from "react";
import { Link } from "react-router-dom";
import { Header } from "../components/Header";
import { UploadDropzone } from "../components/UploadDropzone";
import { StatusBadge } from "../components/StatusBadge";
import { LoadingSpinner } from "../components/LoadingSpinner";
import { uploadVideo, type UploadResponse } from "../api/videoApi";
import { extractErrorMessage } from "../api/client";

function generateIdempotencyKey(): string {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}

const RESOLUTION_OPTIONS = ["1080p", "720p", "480p"] as const;

export const Upload: React.FC = () => {
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [uploadPct, setUploadPct] = useState(0);
  const [result, setResult] = useState<UploadResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [resolutions, setResolutions] = useState<string[]>(["1080p", "720p", "480p"]);
  const [generateThumbnail, setGenerateThumbnail] = useState(true);
  const [extractMetadata, setExtractMetadata] = useState(true);

  const toggleResolution = (res: string) => {
    setResolutions((prev) =>
      prev.includes(res) ? prev.filter((r) => r !== res) : [...prev, res]
    );
  };

  const handleUpload = async () => {
    if (!file) return;
    setUploading(true);
    setError(null);
    setUploadPct(0);
    const key = generateIdempotencyKey();
    try {
      const res = await uploadVideo(file, key, (pct) => setUploadPct(pct));
      setResult(res);
      setFile(null);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setUploading(false);
    }
  };

  const handleReset = () => {
    setResult(null);
    setError(null);
    setFile(null);
    setUploadPct(0);
  };

  return (
    <div className="flex-1 overflow-y-auto">
      <Header
        title="Upload Video"
        subtitle="Upload a video and send it to the processing queue."
      />

      <div className="p-6 max-w-2xl">
        {/* Success state */}
        {result && (
          <div className="card border-green-200 mb-4">
            <div className="px-4 py-3 border-b border-green-100 bg-green-50">
              <div className="text-sm font-medium text-green-800">Upload accepted</div>
            </div>
            <div className="p-4 space-y-3">
              <div className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
                <span className="text-slate-500">Job ID</span>
                <span className="mono text-slate-800 break-all">{result.jobId}</span>
                <span className="text-slate-500">Video ID</span>
                <span className="mono text-slate-800 break-all">{result.videoId}</span>
                <span className="text-slate-500">Status</span>
                <div>
                  <StatusBadge status="QUEUED" />
                </div>
              </div>
              <div className="flex gap-2 pt-2">
                <Link
                  id="view-uploaded-job-btn"
                  to={`/jobs/${result.jobId}`}
                  className="btn-primary"
                >
                  View Job
                </Link>
                <button
                  id="upload-another-btn"
                  onClick={handleReset}
                  className="btn-secondary"
                >
                  Upload Another
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Upload form */}
        {!result && (
          <div className="space-y-4">
            {/* File selection */}
            <div className="card">
              <div className="px-4 py-3 border-b border-slate-200">
                <h2 className="section-title">Select File</h2>
              </div>
              <div className="p-4">
                <UploadDropzone
                  onFile={setFile}
                  selectedFile={file}
                  onClear={() => setFile(null)}
                  disabled={uploading}
                />
              </div>
            </div>

            {/* Processing options */}
            {file && !uploading && (
              <div className="card">
                <div className="px-4 py-3 border-b border-slate-200">
                  <h2 className="section-title">Processing Options</h2>
                </div>
                <div className="p-4 space-y-4">
                  <div>
                    <div className="label mb-2">Output Quality</div>
                    <div className="space-y-1.5">
                      {RESOLUTION_OPTIONS.map((res) => (
                        <label
                          key={res}
                          className="flex items-center gap-2.5 text-sm text-slate-700 cursor-pointer"
                        >
                          <input
                            type="checkbox"
                            checked={resolutions.includes(res)}
                            onChange={() => toggleResolution(res)}
                            className="rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                          />
                          {res}
                        </label>
                      ))}
                    </div>
                    <p className="text-xs text-slate-400 mt-1.5">
                      Resolutions above the source video height are skipped automatically.
                    </p>
                  </div>

                  <div>
                    <div className="label mb-2">Additional Options</div>
                    <div className="space-y-1.5">
                      <label className="flex items-center gap-2.5 text-sm text-slate-700 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={generateThumbnail}
                          onChange={(e) => setGenerateThumbnail(e.target.checked)}
                          className="rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                        />
                        Generate Thumbnail
                      </label>
                      <label className="flex items-center gap-2.5 text-sm text-slate-700 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={extractMetadata}
                          onChange={(e) => setExtractMetadata(e.target.checked)}
                          className="rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                        />
                        Extract Metadata
                      </label>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Error */}
            {error && (
              <div className="text-sm text-red-600 bg-red-50 border border-red-200 rounded px-3 py-2">
                {error}
              </div>
            )}

            {/* Upload progress */}
            {uploading && (
              <div className="card p-4">
                <div className="flex items-center gap-3 mb-3">
                  <LoadingSpinner size="sm" />
                  <span className="text-sm font-medium text-slate-700">Uploading video...</span>
                </div>
                <div>
                  <div className="flex items-center justify-between text-xs text-slate-500 mb-1">
                    <span>{file?.name}</span>
                    <span>{uploadPct}%</span>
                  </div>
                  <div className="h-1.5 bg-slate-100 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-blue-600 transition-all duration-150"
                      style={{ width: `${uploadPct}%` }}
                    />
                  </div>
                </div>
              </div>
            )}

            {/* Actions */}
            {!uploading && (
              <div className="flex items-center gap-3">
                <button
                  id="upload-submit-btn"
                  onClick={handleUpload}
                  disabled={!file}
                  className="btn-primary"
                >
                  Start Processing
                </button>
                {file && (
                  <button
                    id="cancel-upload-btn"
                    onClick={() => setFile(null)}
                    className="btn-ghost text-slate-500"
                  >
                    Cancel
                  </button>
                )}
              </div>
            )}

            {/* Info */}
            <div className="text-xs text-slate-400 space-y-0.5">
              <div>The API returns HTTP 202 — the video is queued for asynchronous processing.</div>
              <div>Maximum file size: 1 GB. Supported formats: MP4, MOV, AVI, MKV, WebM.</div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
