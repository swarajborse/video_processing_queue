import React from "react";
import { Header } from "../components/Header";
import { JobTable } from "../components/JobTable";
import { LoadingSpinner } from "../components/LoadingSpinner";
import { EmptyState } from "../components/EmptyState";
import { useJobs } from "../hooks/useJobs";

export const Jobs: React.FC = () => {
  const { jobs, loading, error, refresh } = useJobs();

  return (
    <div className="flex-1 overflow-y-auto">
      <Header
        title="Jobs"
        subtitle="All video processing jobs, sorted by creation time."
      />

      <div className="p-6 max-w-screen-xl">
        {error && (
          <div className="card border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 mb-4">
            <strong>Error:</strong> {error}
          </div>
        )}

        <div className="card">
          <div className="flex items-center justify-between px-4 py-3 border-b border-slate-200">
            <h2 className="section-title">Processing Jobs</h2>
            <div className="flex items-center gap-2">
              {loading && <LoadingSpinner size="sm" />}
              <button
                id="jobs-refresh-btn"
                onClick={refresh}
                disabled={loading}
                className="btn-secondary text-xs"
              >
                Refresh
              </button>
            </div>
          </div>

          <div className="p-4">
            {!loading && jobs.length === 0 && !error ? (
              <EmptyState
                message="No processing jobs yet."
                detail="Upload your first video to get started."
                action={{ label: "Upload Video", to: "/upload" }}
              />
            ) : (
              <JobTable
                jobs={jobs}
                loading={loading}
                showSearch
                showFilter
              />
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
