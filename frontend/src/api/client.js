const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export function apiUrl(path) {
  if (path.startsWith('http://') || path.startsWith('https://')) return path;
  return path.startsWith('/') ? `${API_BASE}${path}` : `${API_BASE}/${path}`;
}

export async function authFetch(url, options = {}) {
  const token = localStorage.getItem('token');
  const headers = {
    ...options.headers,
  };

  if (!(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(apiUrl(url), {
    ...options,
    headers,
  });

  if (res.status === 401) {
    localStorage.removeItem('token');
    // 토큰 만료 시 로그인 페이지로 강제 이동 및 알림
    if (window.location.pathname !== '/login') {
      alert('로그인이 만료되었습니다. 다시 로그인해 주세요.');
      window.location.href = '/login';
    }
  }

  return res;
}

export { API_BASE };

