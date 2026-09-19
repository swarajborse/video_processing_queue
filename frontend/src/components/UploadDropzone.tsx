import React, { useCallback, useRef, useState } from "react";

interface Props {
  onFile: (file: File) => void;
  selectedFile: File | null;
  onClear: () => void;
  disabled?: boolean;
}

const ACCEPTED_TYPES = [
  "video/mp4",
  "video/quicktime",
  "video/x-msvideo",
  "video/x-matroska",
  "video/webm",
  "video/mpeg",
  "video/x-flv",
];

const ACCEPTED_EXTENSIONS = ".mp4,.mov,.avi,.mkv,.webm,.mpeg,.flv";

export const UploadDropzone: React.FC<Props> = ({
  onFile,
  selectedFile,
  onClear,
  disabled,
}) => {
  const inputRef = useRef<HTMLInputElement>(null);
  const [dragOver, setDragOver] = useState(false);
  const [typeError, setTypeError] = useState<string | null>(null);

  const handleFile = useCallback(
    (file: File) => {
      if (!ACCEPTED_TYPES.includes(file.type) && !file.name.match(/\.(mp4|mov|avi|mkv|webm|mpeg|flv)$/i)) {
        setTypeError(`"${file.name}" does not appear to be a video file.`);
        return;
      }
      setTypeError(null);
      onFile(file);
    },
    [onFile]
  );

  const onDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      setDragOver(false);
      const file = e.dataTransfer.files[0];
      if (file) handleFile(file);
    },
    [handleFile]
  );

  const onInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) handleFile(file);
    e.target.value = "";
  };

  const formatSize = (bytes: number) => {
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
    return `${(bytes / (1024 * 1024 * 1024)).toFixed(2)} GB`;
  };

  if (selectedFile) {
    return (
      <div className="card border-2 border-blue-200 p-5">
        <div className="flex items-start justify-between">
          <div>
            <div className="text-sm font-medium text-slate-900 break-all">{selectedFile.name}</div>
            <div className="flex items-center gap-3 mt-1.5">
              <span className="text-xs text-slate-500">{formatSize(selectedFile.size)}</span>
              <span className="text-xs text-slate-400 font-mono">{selectedFile.type || "video"}</span>
            </div>
          </div>
          <button
            type="button"
            id="clear-file-btn"
            onClick={onClear}
            disabled={disabled}
            className="btn-ghost text-slate-400 hover:text-red-500 p-1 ml-4 shrink-0"
            aria-label="Remove file"
          >
            <svg width="14" height="14" viewBox="0 0 14 14" fill="currentColor">
              <path d="M2 2l10 10M12 2L2 12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
            </svg>
          </button>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div
        id="upload-dropzone"
        role="button"
        tabIndex={0}
        onClick={() => !disabled && inputRef.current?.click()}
        onKeyDown={(e) => e.key === "Enter" && !disabled && inputRef.current?.click()}
        onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
        onDragLeave={() => setDragOver(false)}
        onDrop={onDrop}
        className={`
          border-2 border-dashed rounded p-10 text-center cursor-pointer transition-colors duration-100
          ${dragOver ? "border-blue-400 bg-blue-50" : "border-slate-300 bg-slate-50 hover:border-slate-400 hover:bg-white"}
          ${disabled ? "opacity-50 cursor-not-allowed" : ""}
        `}
      >
        <div className="flex flex-col items-center gap-2">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" className="text-slate-400">
            <path d="M12 16V8M8 12l4-4 4 4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
            <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" strokeWidth="1.5" />
          </svg>
          <div className="text-sm text-slate-600">
            <span className="font-medium text-blue-600">Click to select</span> or drag and drop a video file
          </div>
          <div className="text-xs text-slate-400">
            MP4, MOV, AVI, MKV, WebM, MPEG, FLV
          </div>
        </div>
      </div>
      {typeError && (
        <p className="mt-2 text-xs text-red-600">{typeError}</p>
      )}
      <input
        ref={inputRef}
        type="file"
        id="file-input"
        accept={ACCEPTED_EXTENSIONS}
        className="hidden"
        onChange={onInputChange}
        disabled={disabled}
      />
    </div>
  );
};
