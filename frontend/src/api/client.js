const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export function apiUrl(path) {
  return path.startsWith('/') ? `${API_BASE}${path}` : `${API_BASE}/${path}`;
}

export { API_BASE };
