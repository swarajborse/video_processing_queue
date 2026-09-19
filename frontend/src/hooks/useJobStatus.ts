import { useState, useEffect, useRef, useCallback } from "react";
import { getJobStatus, type JobStatusPoll, type JobStatus } from "../api/videoApi";
import { extractErrorMessage } from "../api/client";

const TERMINAL_STATES: JobStatus[] = ["COMPLETED", "FAILED"];
const POLL_INTERVAL_MS = 3000;

interface UseJobStatusReturn {
  status: JobStatusPoll | null;
  loading: boolean;
  error: string | null;
  refresh: () => void;
}

/**
 * Polls the job status endpoint every POLL_INTERVAL_MS while the job is
 * in a non-terminal state (QUEUED or PROCESSING). Stops automatically
 * when the status becomes COMPLETED or FAILED.
 *
 * Designed so the polling logic can be replaced with WebSocket/SSE
 * without changing the hook's public interface.
 */
export function useJobStatus(jobId: string | undefined): UseJobStatusReturn {
  const [status, setStatus] = useState<JobStatusPoll | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const stopPolling = useCallback(() => {
    if (intervalRef.current !== null) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
  }, []);

  const fetchStatus = useCallback(async () => {
    if (!jobId) return;
    try {
      const data = await getJobStatus(jobId);
      setStatus(data);
      setError(null);
      if (TERMINAL_STATES.includes(data.status)) {
        stopPolling();
      }
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [jobId, stopPolling]);

  useEffect(() => {
    if (!jobId) return;

    setLoading(true);
    fetchStatus();

    intervalRef.current = setInterval(fetchStatus, POLL_INTERVAL_MS);

    return () => {
      stopPolling();
    };
  }, [jobId, fetchStatus, stopPolling]);

  return { status, loading, error, refresh: fetchStatus };
}
