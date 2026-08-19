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

const client = {
  get: async (url, config = {}) => {
    let targetUrl = url;
    if (config.params) {
      const searchParams = new URLSearchParams();
      Object.entries(config.params).forEach(([key, val]) => {
        if (val !== undefined && val !== null && val !== '') {
          searchParams.append(key, val);
        }
      });
      const query = searchParams.toString();
      if (query) targetUrl += (targetUrl.includes('?') ? '&' : '?') + query;
    }
    const res = await authFetch(targetUrl, { method: 'GET', ...config });
    const data = res.status !== 204 ? await res.json() : null;
    return { data, status: res.status };
  },
  post: async (url, body, config = {}) => {
    const res = await authFetch(url, { method: 'POST', body: JSON.stringify(body), ...config });
    const data = res.status !== 204 ? await res.json() : null;
    return { data, status: res.status };
  },
  put: async (url, body, config = {}) => {
    const res = await authFetch(url, { method: 'PUT', body: JSON.stringify(body), ...config });
    const data = res.status !== 204 ? await res.json() : null;
    return { data, status: res.status };
  },
  patch: async (url, body, config = {}) => {
    const res = await authFetch(url, { method: 'PATCH', body: body ? JSON.stringify(body) : undefined, ...config });
    const data = res.status !== 204 ? await res.json() : null;
    return { data, status: res.status };
  },
  delete: async (url, config = {}) => {
    const res = await authFetch(url, { method: 'DELETE', ...config });
    const data = res.status !== 204 ? await res.json().catch(() => null) : null;
    return { data, status: res.status };
  }
};

export default client;

