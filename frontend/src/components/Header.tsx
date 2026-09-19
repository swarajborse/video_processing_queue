import React, { useState } from "react";
import { NavLink } from "react-router-dom";

interface Props {
  title: string;
  subtitle?: string;
}

export const Header: React.FC<Props> = ({ title, subtitle }) => {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <>
      {/* Desktop page header */}
      <div className="border-b border-slate-200 bg-white px-6 py-4">
        <h1 className="text-xl font-semibold text-slate-900">{title}</h1>
        {subtitle && <p className="text-sm text-slate-500 mt-0.5">{subtitle}</p>}
      </div>

      {/* Mobile top nav */}
      <div className="md:hidden flex items-center justify-between px-4 py-3 bg-white border-b border-slate-200">
        <div className="text-sm font-semibold text-slate-900">Video Processing Queue</div>
        <button
          id="mobile-menu-toggle"
          className="btn-ghost p-1"
          onClick={() => setMobileOpen((v) => !v)}
          aria-label="Toggle menu"
        >
          <svg width="18" height="18" viewBox="0 0 18 18" fill="currentColor">
            <rect y="3" width="18" height="1.5" rx="0.75" />
            <rect y="8.25" width="18" height="1.5" rx="0.75" />
            <rect y="13.5" width="18" height="1.5" rx="0.75" />
          </svg>
        </button>
      </div>

      {mobileOpen && (
        <nav className="md:hidden bg-white border-b border-slate-200 px-4 py-2 space-y-0.5">
          {[
            { to: "/", label: "Dashboard" },
            { to: "/jobs", label: "Jobs" },
            { to: "/upload", label: "Upload" },
          ].map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === "/"}
              onClick={() => setMobileOpen(false)}
              className={({ isActive }) =>
                `block px-2.5 py-1.5 text-sm rounded-sm ${
                  isActive ? "bg-blue-50 text-blue-700 font-medium" : "text-slate-600"
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
          <div className="my-1.5 border-t border-slate-200" />
          <a
            href={import.meta.env.VITE_GRAFANA_URL ?? "http://localhost:3000"}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1.5 px-2.5 py-1.5 text-sm text-slate-600"
          >
            Monitoring
            <svg width="9" height="9" viewBox="0 0 10 10" fill="none" className="text-slate-400">
              <path d="M3 1H9V7" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round" />
              <path d="M9 1L1 9" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" />
            </svg>
          </a>
        </nav>
      )}
    </>
  );
};
