import axios, { AxiosError } from "axios";

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export const apiClient = axios.create({
  baseURL: BASE_URL,
  timeout: 30_000,
  headers: {
    Accept: "application/json",
  },
});

export interface ApiErrorDetail {
  message: string;
  status?: number;
  error?: string;
}

export function extractErrorMessage(err: unknown): string {
  if (err instanceof AxiosError) {
    const data = err.response?.data;
    if (data) {
      // ApiErrorResponse shape: { timestamp, status, error, message, path }
      if (typeof data.message === "string" && data.message) {
        return data.message;
      }
      // ErrorResponse shape: { status, error, message }
      if (typeof data.error === "string" && data.error) {
        return data.error;
      }
    }
    if (err.code === "ERR_NETWORK" || err.code === "ECONNREFUSED") {
      return "Unable to connect to the processing server. Is the backend running?";
    }
    if (err.response?.status === 413) {
      return "File is too large to upload.";
    }
    if (err.response?.status === 400) {
      return data?.message ?? "Invalid request. Check the file and try again.";
    }
    if (err.response?.status === 404) {
      return "The requested resource was not found.";
    }
    if (err.response?.status === 409) {
      return "A duplicate request was detected.";
    }
    if (err.response?.status === 500) {
      return "An internal server error occurred. Check the backend logs.";
    }
    return err.message ?? "An unexpected error occurred.";
  }
  if (err instanceof Error) {
    return err.message;
  }
  return "An unexpected error occurred.";
}
