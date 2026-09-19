import React from "react";
import { Link } from "react-router-dom";

interface Props {
  message: string;
  detail?: string;
  action?: {
    label: string;
    to: string;
  };
}

export const EmptyState: React.FC<Props> = ({ message, detail, action }) => (
  <div className="py-16 text-center">
    <div className="inline-flex items-center justify-center w-10 h-10 bg-slate-100 rounded mb-4">
      <svg width="18" height="18" viewBox="0 0 18 18" fill="none" className="text-slate-400">
        <rect x="2" y="2" width="14" height="14" rx="1" stroke="currentColor" strokeWidth="1.5" strokeDasharray="3 2" />
        <path d="M6 9h6M9 6v6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
      </svg>
    </div>
    <p className="text-sm font-medium text-slate-700">{message}</p>
    {detail && <p className="text-sm text-slate-400 mt-1">{detail}</p>}
    {action && (
      <div className="mt-4">
        <Link to={action.to} id="empty-state-action" className="btn-primary">
          {action.label}
        </Link>
      </div>
    )}
  </div>
);
