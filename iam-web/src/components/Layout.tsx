import { Outlet } from 'react-router-dom'
import { Sidebar } from './Sidebar'
import type { Modulo } from '../types/iam'
import { useAuth } from '../context/AuthContext'
import { IconLogout, IconShield } from './Icons'

export function Layout({ modulos, onSelect }: { modulos: Modulo[], onSelect?: (m: Modulo, sId?: number) => void }) {
  const { email, roles, tenantId, logout } = useAuth()
  const userInitial = (email || 'U').charAt(0).toUpperCase()

  return (
    <div className="layout">
      <Sidebar modulos={modulos} onSelect={onSelect} />
      <div className="main">
        {/* Topbar */}
        <header className="topbar">
          <div className="topbar-left">
            <span className="topbar-title">Centro de Permisos & Acceso IAM</span>
            <div className="tenant-badge">
              <span className="pulse-dot" />
              Tenant Activo: <b>{tenantId || 'demo'}</b>
            </div>
          </div>

          <div className="topbar-right">
            <div className="user-profile-badge">
              <div className="user-avatar">{userInitial}</div>
              <span style={{ fontWeight: 500 }}>{email}</span>
              <span className="role-tag">{roles.join(', ') || 'USER'}</span>
            </div>

            <button
              className="btn ghost sm"
              onClick={logout}
              title="Cerrar sesión"
              style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
            >
              <IconLogout size={15} />
              <span>Salir</span>
            </button>
          </div>
        </header>

        {/* Content View */}
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
