// formatDuration.ts
export function formatDuration(startIso: string | null, endIso: string | null): string {
  if (!startIso || !endIso) return "�";
  const diff = new Date(endIso).getTime() - new Date(startIso).getTime();
  if (diff < 0) return "�";
  const sec = diff / 1000;
  if (sec < 60) return `${sec.toFixed(1)}s`;
  const min = Math.floor(sec / 60);
  const remSec = Math.round(sec % 60);
  return `${min}m ${remSec}s`;
}

export function formatVideoDuration(seconds: number | null | undefined): string {
  if (seconds == null) return "�";
  const m = Math.floor(seconds / 60);
  const s = Math.round(seconds % 60);
  return m > 0 ? `${m}m ${s}s` : `${s}s`;
}
