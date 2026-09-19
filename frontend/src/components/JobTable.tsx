import React, { useState } from "react";
import { Link } from "react-router-dom";
import type { JobListItem } from "../api/videoApi";
import { StatusBadge } from "./StatusBadge";
import { SkeletonRow } from "./LoadingSpinner";
import { formatRelative, formatDate } from "../utils/formatDate";
import { formatDuration } from "../utils/formatDuration";

interface Props {
  jobs: JobListItem[];
  loading?: boolean;
  showSearch?: boolean;
  showFilter?: boolean;
  compact?: boolean;
}

function truncateId(id: string): string {
  return id.substring(0, 8) + "...";
}

async function copyToClipboard(text: string) {
  try {
    await navigator.clipboard.writeText(text);
  } catch {
    // ignore � clipboard may not be available in all contexts
  }
}

export const JobTable: React.FC<Props> = ({
  jobs,
  loading,
  showSearch = false,
  showFilter = false,
  compact = false,
}) => {
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const filtered = jobs.filter((job) => {
    const matchSearch =
      !search ||
      job.jobId.toLowerCase().includes(search.toLowerCase()) ||
      job.filename.toLowerCase().includes(search.toLowerCase());
    const matchStatus = statusFilter === "ALL" || job.status === statusFilter;
    return matchSearch && matchStatus;
  });

  const handleCopy = async (id: string, e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    await copyToClipboard(id);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 1500);
  };

  return (
    <div>
      {(showSearch || showFilter) && (
        <div className="flex items-center gap-2 mb-3">
          {showSearch && (
            <input
              id="job-search"
              type="text"
              placeholder="Search by job ID or filename..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="input max-w-xs"
            />
          )}
          {showFilter && (
            <select
              id="status-filter"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="select"
            >
              <option value="ALL">All statuses</option>
              <option value="QUEUED">Queued</option>
              <option value="PROCESSING">Processing</option>
              <option value="COMPLETED">Completed</option>
              <option value="FAILED">Failed</option>
            </select>
          )}
          <span className="text-xs text-slate-400 ml-auto">
            {filtered.length} {filtered.length === 1 ? "job" : "jobs"}
          </span>
        </div>
      )}

      <div className="overflow-x-auto">
        <table className="w-full text-sm border-collapse">
          <thead>
            <tr className="border-b border-slate-200">
              <th className="px-3 py-2 text-left text-xs font-medium text-slate-500 uppercase tracking-wide w-32">Job ID</th>
              <th className="px-3 py-2 text-left text-xs font-medium text-slate-500 uppercase tracking-wide">File</th>
              <th className="px-3 py-2 text-left text-xs font-medium text-slate-500 uppercase tracking-wide w-28">Status</th>
              <th className="px-3 py-2 text-left text-xs font-medium text-slate-500 uppercase tracking-wide w-28">Created</th>
              {!compact && (
                <>
                  <th className="px-3 py-2 text-left text-xs font-medium text-slate-500 uppercase tracking-wide w-28">Started</th>
                  <th className="px-3 py-2 text-left text-xs font-medium text-slate-500 uppercase tracking-wide w-28">Completed</th>
                </>
              )}
              <th className="px-3 py-2 text-left text-xs font-medium text-slate-500 uppercase tracking-wide w-24">Duration</th>
              <th className="px-3 py-2 text-right text-xs font-medium text-slate-500 uppercase tracking-wide w-16">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {loading ? (
              Array.from({ length: compact ? 5 : 8 }).map((_, i) => (
                <SkeletonRow key={i} cols={compact ? 6 : 8} />
              ))
            ) : filtered.length === 0 ? (
              <tr>
                <td colSpan={compact ? 6 : 8} className="px-3 py-8 text-center text-sm text-slate-400">
                  {search || statusFilter !== "ALL" ? "No jobs match the current filters." : "No jobs found."}
                </td>
              </tr>
            ) : (
              filtered.map((job) => (
                <tr key={job.jobId} className="table-row-hover">
                  <td className="px-3 py-2">
                    <div className="flex items-center gap-1.5">
                      <span className="mono text-slate-600">{truncateId(job.jobId)}</span>
                      <button
                        id={`copy-${job.jobId}`}
                        onClick={(e) => handleCopy(job.jobId, e)}
                        className="text-slate-300 hover:text-slate-500 transition-colors"
                        title="Copy full ID"
                        aria-label="Copy job ID"
                      >
                        {copiedId === job.jobId ? (
                          <svg width="12" height="12" viewBox="0 0 12 12" fill="currentColor" className="text-green-500">
                            <path d="M2 6l3 3 5-5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" fill="none" />
                          </svg>
                        ) : (
                          <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                            <rect x="4" y="1" width="7" height="7" rx="0.75" stroke="currentColor" strokeWidth="1" />
                            <path d="M1 4v7h7" stroke="currentColor" strokeWidth="1" strokeLinecap="round" />
                          </svg>
                        )}
                      </button>
                    </div>
                  </td>
                  <td className="px-3 py-2">
                    <span className="text-slate-800 max-w-[200px] truncate block" title={job.filename}>
                      {job.filename}
                    </span>
                  </td>
                  <td className="px-3 py-2">
                    <StatusBadge status={job.status} />
                  </td>
                  <td className="px-3 py-2 text-slate-500 text-xs">
                    {formatRelative(job.createdAt)}
                  </td>
                  {!compact && (
                    <>
                      <td className="px-3 py-2 text-slate-500 text-xs">
                        {job.startedAt ? formatRelative(job.startedAt) : <span className="text-slate-300">�</span>}
                      </td>
                      <td className="px-3 py-2 text-slate-500 text-xs">
                        {job.completedAt ? formatRelative(job.completedAt) : <span className="text-slate-300">�</span>}
                      </td>
                    </>
                  )}
                  <td className="px-3 py-2 text-slate-500 text-xs mono">
                    {formatDuration(job.startedAt, job.completedAt)}
                  </td>
                  <td className="px-3 py-2 text-right">
                    <Link
                      to={`/jobs/${job.jobId}`}
                      id={`view-job-${job.jobId}`}
                      className="text-blue-600 hover:text-blue-700 text-xs font-medium hover:underline"
                    >
                      View
                    </Link>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
