import { createContext, useContext, useEffect, useState } from 'react'
import type { AuthState } from '../types/iam'

type Ctx = AuthState & {
  login: (email: string, tenantCodigo: string, role: string) => void
  logout: () => void
  isSuperAdmin: boolean
  isTenantAdmin: boolean
}

const AuthContext = createContext<Ctx | null>(null)

function decodeMock(token: string): Partial<AuthState> {
  try {
    const payload = JSON.parse(atob(token.split('.')[1] || ''))
    return {
      tenantId: payload.tenant_id ?? null,
      userId: payload.user_id ?? null,
      roles: payload.roles ?? [],
      email: payload.email ?? null,
    }
  } catch { return {} }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<AuthState>(() => {
    const t = localStorage.getItem('iam_token')
    if (!t) return { token: null, tenantId: 1, userId: 1, roles: ['TENANT_ADMIN'], email: 'admin@demo.local' }
    const d = decodeMock(t)
    return { token: t, tenantId: d.tenantId ?? 1, userId: d.userId ?? 1, roles: d.roles ?? ['TENANT_ADMIN'], email: d.email ?? 'admin@demo.local' }
  })

  useEffect(() => {
    if (state.token) localStorage.setItem('iam_token', state.token)
  }, [state.token])

  const login = (email: string, tenantCodigo: string, role: string) => {
    // Mock JWT — en prod: POST /oauth2/token con grant_type=password o authorization_code
    // payload base64: {"tenant_id":1,"user_id":1,"roles":["TENANT_ADMIN"],"email":...}
    const payload = btoa(JSON.stringify({ tenant_id: 1, user_id: 1, roles: [role], email, tenantCodigo }))
    const mock = `header.${payload}.signature`
    localStorage.setItem('iam_token', mock)
    setState({ token: mock, tenantId: 1, userId: 1, roles: [role], email })
  }

  const logout = () => {
    localStorage.removeItem('iam_token')
    setState({ token: null, tenantId: null, userId: null, roles: [], email: null })
  }

  return (
    <AuthContext.Provider value={{
      ...state,
      login, logout,
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
