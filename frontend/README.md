# Video Processing Queue — Frontend

A developer-oriented internal dashboard for the Video Processing Queue backend system.

Built with **React + Vite + TypeScript + Tailwind CSS**.

## Getting Started

### Prerequisites

- Node.js 18+
- The backend API running (see `../api/`)

### Setup

```bash
cd frontend
cp .env.example .env       # adjust VITE_API_BASE_URL if needed
npm install
npm run dev
```

The app starts at http://localhost:5173.

### Build

```bash
npm run build
```

Production output is in `dist/`.

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080` | Backend API base URL |

## Project Structure

```
src/
  api/
    client.ts       — Axios instance and error message extraction
    videoApi.ts     — All API functions and TypeScript types
  hooks/
    useJobs.ts      — Fetches and refreshes the jobs list
    useJobStatus.ts — Polls job status every 3s (stops on COMPLETED/FAILED)
  components/
    Sidebar.tsx     — Persistent left sidebar with API health check
    Header.tsx      — Page header + mobile nav
    StatusBadge.tsx — QUEUED / PROCESSING / COMPLETED / FAILED badge
    StatCard.tsx    — Compact stat card for the dashboard
    JobTable.tsx    — Reusable jobs table with search and status filter
    UploadDropzone  — Drag-and-drop file selector
    EmptyState.tsx  — Empty list state with optional CTA
    LoadingSpinner  — Spinner and skeleton rows
  pages/
    Dashboard.tsx   — Overview: stats + recent jobs
    Jobs.tsx        — Full job list with search + filter
    Upload.tsx      — File upload with progress and 202 handling
    JobDetails.tsx  — Job detail view with live status polling + outputs
  utils/
    formatDate.ts   — Date formatting and relative time
    formatDuration.ts — Processing duration formatting
    formatFileSize.ts — Human-readable byte sizes
```

## API Endpoints Used

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/videos/upload` | Upload a video (multipart, requires `Idempotency-Key` header) |
| `GET` | `/api/videos/jobs` | List all processing jobs |
| `GET` | `/api/videos/jobs/:id` | Get full job detail with outputs |
| `GET` | `/api/videos/jobs/:id/status` | Lightweight status poll |

## Design Principles

- No fake data, no hardcoded statistics
- Status polling stops automatically when job reaches COMPLETED or FAILED
- All error messages are translated to human-readable strings
- Tables where tables make sense; no decorative illustrations
