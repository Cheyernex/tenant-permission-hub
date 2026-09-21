const API_BASE = import.meta.env.VITE_API_BASE || ''
const AUTH_BASE = import.meta.env.VITE_AUTH_BASE || ''

function getToken() {
  return localStorage.getItem('iam_token')
}

export async function apiFetch(path: string, opts: RequestInit = {}) {
  const token = getToken()
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(opts.headers as Record<string, string> || {}),
  }
  if (token) headers['Authorization'] = `Bearer ${token}`

  const res = await fetch(`${API_BASE}${path}`, { ...opts, headers })

  // Si es 401/403 y estamos en modo mock, no romper la UI
  if (!res.ok) {
    const body = await res.text().catch(() => '')
    throw new Error(`${res.status} ${res.statusText} ${body}`.trim())
  }
  const text = await res.text()
  return text ? JSON.parse(text) : null
}

export async function authFetch(path: string, opts: RequestInit = {}) {
  return fetch(`${AUTH_BASE}${path}`, opts)
}

// Endpoints helpers — caen a mock si el backend no está disponible
export const endpoints = {
  modulos: '/api/v1/modulos',
  submodulos: (moduloId: number) => `/api/v1/modulos/${moduloId}/submodulos`,
  acciones: (submoduloId: number) => `/api/v1/submodulos/${submoduloId}/acciones`,
  usuarios: '/api/v1/usuarios',
  plantillas: '/api/v1/plantillas',
}
