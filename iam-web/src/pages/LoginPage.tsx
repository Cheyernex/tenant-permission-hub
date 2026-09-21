import { useState } from 'react'
import { useAuth } from '../context/AuthContext'

export function LoginPage() {
  const { login, error } = useAuth()
  const [email, setEmail] = useState('admin@demo.local')
  const [password, setPassword] = useState('password123')
  const [tenant, setTenant] = useState('demo')
  const [role, setRole] = useState('TENANT_ADMIN')
  const [busy, setBusy] = useState(false)
  const [localError, setLocalError] = useState<string | null>(null)

  const handle = async () => {
    setBusy(true); setLocalError(null)
    try { await login(email, tenant, password, role) }
    catch(e:any){ setLocalError(e.message) } finally { setBusy(false) }
  }

  return (
    <div className="login-wrap">
      <div className="login-card">
        <h1>Tenant Permission Hub</h1>
        <p className="muted">Login real contra <code>POST http://localhost:9000/dev/token</code> — emite JWT firmado por Auth Server (<code>tenant_id, user_id, roles, permissions</code> vía <code>PermissionCacheService</code>). Sin mocks.</p>
        <label>Email<input value={email} onChange={e=>setEmail(e.target.value)} /></label>
        <label>Password<input type="password" value={password} onChange={e=>setPassword(e.target.value)} /></label>
        <label>Tenant código<input value={tenant} onChange={e=>setTenant(e.target.value)} /></label>
        <label>Rol
          <select value={role} onChange={e=>setRole(e.target.value)}>
            <option>TENANT_ADMIN</option>
            <option>SUPER_ADMIN</option>
            <option>USER</option>
          </select>
        </label>
        {(error || localError) && <div className="callout" style={{borderColor:'#f87171'}}>{error || localError}</div>}
        <button className="btn primary block" onClick={handle} disabled={busy}>{busy ? 'Autenticando…' : 'Entrar (JWT real)'}</button>
        <div className="callout sm">Requiere <code>docker-compose up -d postgres redis auth-server</code>. Si el usuario no existe, el dev endpoint crea token con <code>tenant 1</code> y role solicitado (fallback dev).</div>
      </div>
    </div>
  )
}
