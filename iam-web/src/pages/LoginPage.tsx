import { useState } from 'react'
import { useAuth } from '../context/AuthContext'

export function LoginPage() {
  const { login } = useAuth()
  const [email, setEmail] = useState('admin@demo.local')
  const [tenant, setTenant] = useState('demo')
  const [role, setRole] = useState('TENANT_ADMIN')

  return (
    <div className="login-wrap">
      <div className="login-card">
        <h1>Tenant Permission Hub</h1>
        <p className="muted">Demo login — en prod usa <code>POST /oauth2/token</code> (Auth Server :9000) y guarda el JWT con <code>tenant_id</code> + <code>permissions</code>.</p>
        <label>Email<input value={email} onChange={e=>setEmail(e.target.value)} /></label>
        <label>Tenant código<input value={tenant} onChange={e=>setTenant(e.target.value)} /></label>
        <label>Rol
          <select value={role} onChange={e=>setRole(e.target.value)}>
            <option>TENANT_ADMIN</option>
            <option>SUPER_ADMIN</option>
            <option>USER</option>
          </select>
        </label>
        <button className="btn primary block" onClick={()=>login(email, tenant, role)}>Entrar (mock JWT)</button>
        <div className="callout sm">El sidebar se genera dinámicamente desde <code>GET /api/v1/modulos</code> (o seed si no hay backend). Crea módulos/submódulos/acciones y verás el cambio al instante.</div>
      </div>
    </div>
  )
}
