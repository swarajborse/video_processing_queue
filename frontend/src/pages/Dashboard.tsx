import React from "react";
import { Link } from "react-router-dom";
import { Header } from "../components/Header";
import { StatCard } from "../components/StatCard";
import { JobTable } from "../components/JobTable";
import { EmptyState } from "../components/EmptyState";
import { LoadingSpinner } from "../components/LoadingSpinner";
import { useJobs } from "../hooks/useJobs";

const GRAFANA_URL = import.meta.env.VITE_GRAFANA_URL ?? "http://localhost:3000";

export const Dashboard: React.FC = () => {
  const { jobs, loading, error, refresh } = useJobs();

  const stats = {
    total: jobs.length,
    queued: jobs.filter((j) => j.status === "QUEUED").length,
    processing: jobs.filter((j) => j.status === "PROCESSING").length,
    completed: jobs.filter((j) => j.status === "COMPLETED").length,
    failed: jobs.filter((j) => j.status === "FAILED").length,
  };

  const recentJobs = jobs.slice(0, 10);

  return (
    <div className="flex-1 overflow-y-auto">
      <Header
        title="Dashboard"
        subtitle="Monitor video processing jobs and worker activity."
      />

      <div className="p-6 space-y-6 max-w-screen-xl">
        {/* Stats Row */}
        <div className="flex items-start justify-between gap-4">
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3 flex-1">
            <StatCard id="stat-total" label="Total Jobs" value={loading ? "\u2014" : stats.total} />
            <StatCard id="stat-queued" label="Queued" value={loading ? "\u2014" : stats.queued} />
            <StatCard id="stat-processing" label="Processing" value={loading ? "\u2014" : stats.processing} />
            <StatCard id="stat-completed" label="Completed" value={loading ? "\u2014" : stats.completed} />
            <StatCard id="stat-failed" label="Failed" value={loading ? "\u2014" : stats.failed} />
          </div>
          <a
            href={GRAFANA_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="text-xs text-slate-400 hover:text-slate-600 shrink-0 mt-1 flex items-center gap-1"
          >
            View system monitoring
            <svg width="9" height="9" viewBox="0 0 10 10" fill="none">
              <path d="M3 1H9V7" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round" />
              <path d="M9 1L1 9" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" />
            </svg>
          </a>
        </div>

        {/* Error */}
        {error && (
          <div className="card border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            <strong>Error loading jobs:</strong> {error}
          </div>
        )}

        {/* Recent Jobs */}
        <div className="card">
          <div className="flex items-center justify-between px-4 py-3 border-b border-slate-200">
            <h2 className="section-title">Recent Jobs</h2>
            <div className="flex items-center gap-2">
              {loading && <LoadingSpinner size="sm" />}
              <button
                id="refresh-btn"
                onClick={refresh}
                disabled={loading}
                className="btn-secondary text-xs"
              >
                Refresh
              </button>
              <Link to="/jobs" className="btn-ghost text-xs text-blue-600">
                View all
              </Link>
            </div>
          </div>
          <div className="p-4">
            {!loading && jobs.length === 0 && !error ? (
              <EmptyState
                message="No processing jobs yet."
                detail="Upload a video to start your first processing job."
                action={{ label: "Upload Video", to: "/upload" }}
              />
            ) : (
              <JobTable jobs={recentJobs} loading={loading} compact />
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
