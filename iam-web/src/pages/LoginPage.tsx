import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import {
  IconShield,
  IconLock,
  IconKey,
  IconDatabase,
  IconServer,
  IconCheck,
  IconSparkles
} from '../components/Icons'

export function LoginPage() {
  const { login, error } = useAuth()
  const [email, setEmail] = useState('superadmin@system.local')
  const [password, setPassword] = useState('password')
  const [tenant, setTenant] = useState('system')
  const [role, setRole] = useState('SUPER_ADMIN')
  const [busy, setBusy] = useState(false)
  const [localError, setLocalError] = useState<string | null>(null)

  const handle = async (
    eEmail = email,
    eTenant = tenant,
    ePass = password,
    eRole = role
  ) => {
    setBusy(true)
    setLocalError(null)
    try {
      await login(eEmail, eTenant, ePass, eRole)
    } catch (e: any) {
      setLocalError(e.message)
    } finally {
      setBusy(false)
    }
  }

  const selectPreset = (
    pEmail: string,
    pTenant: string,
    pPass: string,
    pRole: string
  ) => {
    setEmail(pEmail)
    setTenant(pTenant)
    setPassword(pPass)
    setRole(pRole)
    handle(pEmail, pTenant, pPass, pRole)
  }

  const isSuperSelected = email === 'superadmin@system.local' && role === 'SUPER_ADMIN'
  const isTenantSelected = email === 'admin@demo.local' && role === 'TENANT_ADMIN'

  return (
    <div className="login-wrap">
      <div className="login-card">
        {/* Brand Header */}
        <div className="login-brand">
          <div className="login-brand-icon">
            <IconShield size={24} color="#ffffff" />
          </div>
          <div>
            <h1>Tenant Permission Hub</h1>
            <div style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span className="pulse-dot" style={{ width: '5px', height: '5px' }} />
              OAuth2 & JWT Auth Gateway
            </div>
          </div>
        </div>

        <p className="page-desc" style={{ fontSize: '12.5px', marginBottom: '16px' }}>
          <b>Persistencia Real PostgreSQL</b> — Autenticación contra <code>Auth Server :9000 /dev/token</code> con emisión de JWT criptográfico (<code>tenant_id</code> + permisos en Redis).
        </p>

        {/* Quick Demo Credentials Presets */}
        <div style={{ marginBottom: '16px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '8px' }}>
            Acceso Rápido con Datos Semilla (V5__demo_seed):
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
            <button
              type="button"
              className={`user-card-item ${isSuperSelected ? 'active' : ''}`}
              style={{ padding: '8px 10px', fontSize: '11.5px', borderRadius: '8px' }}
              onClick={() => selectPreset('superadmin@system.local', 'system', 'password', 'SUPER_ADMIN')}
              disabled={busy}
            >
              <div className="user-item-avatar" style={{ width: '28px', height: '28px', fontSize: '11px', background: 'var(--grad-primary)' }}>
                S
              </div>
              <div style={{ textAlign: 'left', minWidth: 0, flex: 1 }}>
                <div style={{ fontWeight: 700, color: '#ffffff', fontSize: '12px' }}>SUPER_ADMIN</div>
                <div className="muted sm" style={{ fontSize: '10.5px' }}>system · global</div>
              </div>
            </button>

            <button
              type="button"
              className={`user-card-item ${isTenantSelected ? 'active' : ''}`}
              style={{ padding: '8px 10px', fontSize: '11.5px', borderRadius: '8px' }}
              onClick={() => selectPreset('admin@demo.local', 'demo', 'password', 'TENANT_ADMIN')}
              disabled={busy}
            >
              <div className="user-item-avatar" style={{ width: '28px', height: '28px', fontSize: '11px', background: 'var(--grad-emerald)' }}>
                T
              </div>
              <div style={{ textAlign: 'left', minWidth: 0, flex: 1 }}>
                <div style={{ fontWeight: 700, color: '#ffffff', fontSize: '12px' }}>TENANT_ADMIN</div>
                <div className="muted sm" style={{ fontSize: '10.5px' }}>demo · tenant</div>
              </div>
            </button>
          </div>
        </div>

        {/* Form Inputs */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div>
            <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-sub)', marginBottom: '4px', display: 'block' }}>
              Correo Electrónico
            </label>
            <input
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              placeholder="admin@demo.local"
              style={{ width: '100%' }}
              disabled={busy}
            />
          </div>

          <div>
            <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-sub)', marginBottom: '4px', display: 'block' }}>
              Contraseña
            </label>
            <input
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              placeholder="••••••••••••"
              style={{ width: '100%' }}
              disabled={busy}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.2fr', gap: '10px' }}>
            <div>
              <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-sub)', marginBottom: '4px', display: 'block' }}>
                Código de Tenant
              </label>
              <input
                value={tenant}
                onChange={e => setTenant(e.target.value)}
                placeholder="demo o system"
                style={{ width: '100%' }}
                disabled={busy}
              />
            </div>

            <div>
              <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-sub)', marginBottom: '4px', display: 'block' }}>
                Rol de Acceso
              </label>
              <select
                value={role}
                onChange={e => setRole(e.target.value)}
                style={{ width: '100%' }}
                disabled={busy}
              >
                <option value="SUPER_ADMIN">SUPER_ADMIN (Global)</option>
                <option value="TENANT_ADMIN">TENANT_ADMIN (Por Tenant)</option>
                <option value="USER">USER (Estándar)</option>
              </select>
            </div>
          </div>
        </div>

        {/* Error Alert */}
        {(error || localError) && (
          <div
            className="callout"
            style={{
              background: 'rgba(244, 63, 94, 0.1)',
              borderColor: 'rgba(244, 63, 94, 0.35)',
              color: '#fca5a5',
              marginTop: '16px',
              fontSize: '12px'
            }}
          >
            <div>
              <b>Error de autenticación:</b> {error || localError}
            </div>
          </div>
        )}

        {/* Submit Button */}
        <button
          className="btn primary block"
          style={{ marginTop: '18px', padding: '12px', fontSize: '14px' }}
          onClick={() => handle()}
          disabled={busy}
        >
          {busy ? (
            <>
              <span className="pulse-dot" style={{ background: '#ffffff', boxShadow: 'none' }} />
              Autenticando con Auth Server…
            </>
          ) : (
            <>
              <IconKey size={16} />
              Iniciar Sesión (JWT Real)
            </>
          )}
        </button>

        {/* Info Box */}
        <div className="callout info" style={{ marginTop: '18px', fontSize: '11.5px', lineHeight: 1.5 }}>
          <IconSparkles size={16} style={{ flexShrink: 0, marginTop: '2px' }} />
          <div>
            <b>Aviso de permisos:</b> Los <code>SUPER_ADMIN</code> tienen permisos globales de escritura en Módulos. Los <code>TENANT_ADMIN</code> gestionan usuarios y plantillas exclusivamente dentro de su tenant.
          </div>
        </div>
      </div>
    </div>
  )
}
