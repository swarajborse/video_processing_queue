/** @type {import("tailwindcss").Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Inter", "system-ui", "sans-serif"],
        mono: ["JetBrains Mono", "Menlo", "monospace"],
      },
      colors: {
        border: "#e2e8f0",
        background: "#f8f9fa",
        surface: "#ffffff",
        "text-primary": "#0f172a",
        "text-secondary": "#64748b",
        "text-muted": "#94a3b8",
        primary: {
          DEFAULT: "#2563eb",
          hover: "#1d4ed8",
          light: "#eff6ff",
        },
        status: {
          queued: { bg: "#f1f5f9", text: "#475569" },
          processing: { bg: "#fffbeb", text: "#d97706" },
          completed: { bg: "#f0fdf4", text: "#16a34a" },
          failed: { bg: "#fef2f2", text: "#dc2626" },
        },
      },
      fontSize: {
        "2xs": ["0.7rem", { lineHeight: "1rem" }],
      },
    },
  },
  plugins: [],
};
