const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export function apiUrl(path) {
  return path.startsWith('/') ? `${API_BASE}${path}` : `${API_BASE}/${path}`;
}

export async function authFetch(url, options = {}) {
  const token = localStorage.getItem('token');
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(apiUrl(url), {
    ...options,
    headers,
  });

  if (res.status === 401) {
    // 토큰 만료 시 로그아웃 처리 가능 (여기선 단순히 에러 던짐)
    localStorage.removeItem('token');
    window.location.href = '/login';
  }

  return res;
}

export { API_BASE };

