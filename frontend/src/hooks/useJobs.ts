import { useState, useEffect, useCallback } from "react";
import { getJobs, type JobListItem } from "../api/videoApi";
import { extractErrorMessage } from "../api/client";

interface UseJobsReturn {
  jobs: JobListItem[];
  loading: boolean;
  error: string | null;
  refresh: () => void;
}

export function useJobs(): UseJobsReturn {
  const [jobs, setJobs] = useState<JobListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getJobs();
      setJobs(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return { jobs, loading, error, refresh: load };
}
