import React from "react";

interface Props {
  label: string;
  value: string | number;
  id?: string;
}

export const StatCard: React.FC<Props> = ({ label, value, id }) => (
  <div className="stat-card" id={id}>
    <div className="label mb-1">{label}</div>
    <div className="text-2xl font-semibold text-slate-900 tabular-nums">{value}</div>
  </div>
);
