import React from "react";
import type { JobStatus } from "../api/videoApi";

interface Props {
  status: JobStatus;
}

const labels: Record<JobStatus, string> = {
  QUEUED: "Queued",
  PROCESSING: "Processing",
  COMPLETED: "Completed",
  FAILED: "Failed",
};

const classes: Record<JobStatus, string> = {
  QUEUED: "badge-queued",
  PROCESSING: "badge-processing",
  COMPLETED: "badge-completed",
  FAILED: "badge-failed",
};

export const StatusBadge: React.FC<Props> = ({ status }) => (
  <span className={classes[status]}>
    {labels[status]}
  </span>
);
