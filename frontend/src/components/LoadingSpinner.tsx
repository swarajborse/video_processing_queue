import React from "react";

interface Props {
  text?: string;
  size?: "sm" | "md";
}

export const LoadingSpinner: React.FC<Props> = ({ text, size = "md" }) => {
  const sz = size === "sm" ? "w-4 h-4" : "w-5 h-5";
  return (
    <div className="flex items-center gap-2 text-slate-500 text-sm">
      <svg
        className={`${sz} animate-spin`}
        viewBox="0 0 24 24"
        fill="none"
      >
        <circle
          className="opacity-25"
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          strokeWidth="3"
        />
        <path
          className="opacity-75"
          fill="currentColor"
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
        />
      </svg>
      {text && <span>{text}</span>}
    </div>
  );
};

export const SkeletonRow: React.FC<{ cols?: number }> = ({ cols = 6 }) => (
  <tr>
    {Array.from({ length: cols }).map((_, i) => (
      <td key={i} className="px-3 py-2">
        <div className="h-3.5 bg-slate-100 rounded animate-pulse" style={{ width: `${60 + Math.random() * 40}%` }} />
      </td>
    ))}
  </tr>
);
