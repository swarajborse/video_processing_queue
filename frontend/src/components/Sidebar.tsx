import React from "react";
import { NavLink } from "react-router-dom";
import { useState, useEffect } from "react";

interface NavItem {
  to: string;
  label: string;
}

const NAV: NavItem[] = [
  { to: "/", label: "Dashboard" },
  { to: "/jobs", label: "Jobs" },
  { to: "/upload", label: "Upload" },
];

const GRAFANA_URL = import.meta.env.VITE_GRAFANA_URL ?? "http://localhost:3000";

export const Sidebar: React.FC = () => {
  const [apiOnline, setApiOnline] = useState<boolean | null>(null);

  useEffect(() => {
    const check = async () => {
      try {
        const res = await fetch("/api/videos/jobs", { signal: AbortSignal.timeout(3000) });
        setApiOnline(res.status < 500);
      } catch {
        setApiOnline(false);
      }
    };
    check();
    const id = setInterval(check, 15_000);
    return () => clearInterval(id);
  }, []);

  return (
    <aside className="hidden md:flex flex-col w-52 shrink-0 border-r border-slate-200 bg-white h-screen sticky top-0">
      {/* Logo / Brand */}
      <div className="px-4 py-4 border-b border-slate-200">
        <div className="flex items-center gap-2">
          <span className="w-5 h-5 bg-blue-600 rounded-sm flex items-center justify-center">
            <svg width="10" height="10" viewBox="0 0 10 10" fill="none">
              <rect x="1" y="1" width="3" height="3" fill="white" />
              <rect x="6" y="1" width="3" height="3" fill="white" />
              <rect x="1" y="6" width="3" height="3" fill="white" />
              <rect x="6" y="6" width="3" height="3" fill="white" />
            </svg>
          </span>
          <div>
            <div className="text-xs font-semibold text-slate-900 leading-tight">Video Processing</div>
            <div className="text-2xs text-slate-400">Queue System</div>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 py-3 px-2 space-y-0.5">
        {NAV.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === "/"}
            className={({ isActive }) =>
              `block px-2.5 py-1.5 text-sm rounded-sm transition-colors duration-75 ${
                isActive
                  ? "bg-blue-50 text-blue-700 font-medium"
                  : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
              }`
            }
          >
            {item.label}
          </NavLink>
        ))}

        <div className="my-2 border-t border-slate-200" />

        <a
          href={GRAFANA_URL}
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center justify-between px-2.5 py-1.5 text-sm text-slate-600 hover:bg-slate-100 hover:text-slate-900 rounded-sm transition-colors duration-75"
        >
          <span>Monitoring</span>
          <svg width="10" height="10" viewBox="0 0 10 10" fill="none" className="text-slate-400 shrink-0">
            <path d="M3 1H9V7" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round" />
            <path d="M9 1L1 9" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" />
          </svg>
        </a>
      </nav>

      {/* System Status */}
      <div className="px-4 py-3 border-t border-slate-200">
        <div className="label mb-1.5">System</div>
        <div className="flex items-center gap-1.5 text-xs text-slate-500">
          <span
            className={`w-1.5 h-1.5 rounded-full ${
              apiOnline === null
                ? "bg-slate-300"
                : apiOnline
                ? "bg-green-500"
                : "bg-red-500"
            }`}
          />
          <span>
            API:{" "}
            {apiOnline === null
              ? "Checking..."
              : apiOnline
              ? "Connected"
              : "Disconnected"}
          </span>
        </div>
      </div>
    </aside>
  );
};
