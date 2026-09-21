import { createContext, useContext, useEffect, useState } from 'react'
import type { AuthState } from '../types/iam'

type Ctx = AuthState & {
  login: (email: string, tenantCodigo: string, password: string, role: string) => Promise<void>
  logout: () => void
  isSuperAdmin: boolean
  isTenantAdmin: boolean
  error: string | null
}

const AuthContext = createContext<Ctx | null>(null)

const AUTH_BASE = (import.meta.env.VITE_AUTH_BASE as string) || 'http://localhost:9000'

function decode(token: string): Partial<AuthState> {
  try {
    const payload = JSON.parse(atob(token.split('.')[1] || ''))
    return {
      tenantId: payload.tenant_id ?? payload.tenantId ?? null,
      userId: payload.user_id ?? payload.userId ?? null,
      roles: payload.roles ?? [],
      email: payload.email ?? null,
    }
  } catch { return {} }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<AuthState>(() => {
    const t = localStorage.getItem('iam_token')
    if (!t) return { token: null, tenantId: null, userId: null, roles: [], email: null }
    const d = decode(t)
    return { token: t, tenantId: d.tenantId ?? null, userId: d.userId ?? null, roles: d.roles ?? [], email: d.email ?? null }
  })
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (state.token) localStorage.setItem('iam_token', state.token)
    else localStorage.removeItem('iam_token')
  }, [state.token])

  const login = async (email: string, tenantCodigo: string, password: string, role: string) => {
    setError(null)
    // Intento real contra auth-server dev endpoint
    try {
      const res = await fetch(`${AUTH_BASE}/dev/token`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password, tenantCodigo, role }),
      })
      if (!res.ok) {
        const txt = await res.text()
        throw new Error(txt || `HTTP ${res.status}`)
      }
      const data = await res.json()
      const token: string = data.access_token
      if (!token) throw new Error('Respuesta sin access_token')
      const d = decode(token)
      setState({ token, tenantId: d.tenantId ?? null, userId: d.userId ?? null, roles: d.roles ?? [], email: d.email ?? email })
      return
    } catch (e: any) {
      setError(e.message || 'No se pudo conectar con Auth Server :9000. Verifica docker-compose up.')
      throw e
    }
  }

  const logout = () => {
    setState({ token: null, tenantId: null, userId: null, roles: [], email: null })
  }

  return (
    <AuthContext.Provider value={{
      ...state,
      login, logout, error,
      isSuperAdmin: state.roles.includes('SUPER_ADMIN'),
      isTenantAdmin: state.roles.includes('TENANT_ADMIN'),
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const v = useContext(AuthContext)
  if (!v) throw new Error('useAuth fuera de provider')
  return v
}
