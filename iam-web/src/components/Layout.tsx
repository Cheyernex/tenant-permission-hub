import { Outlet } from 'react-router-dom'
import { Sidebar } from './Sidebar'
import type { Modulo } from '../types/iam'
import { useAuth } from '../context/AuthContext'

export function Layout({ modulos, onSelect }: { modulos: Modulo[], onSelect?: (m: Modulo, sId?: number) => void }) {
  const { email, roles, tenantId, logout } = useAuth()
  return (
    <div className="layout">
      <Sidebar modulos={modulos} onSelect={onSelect} />
      <div className="main">
        <header className="topbar">
          <div className="topbar-left">
            <span className="topbar-title">Panel de permisos modulares</span>
            <span className="topbar-sub">tenant_id <b>{tenantId}</b> · JWT con permisos efectivos</span>
          </div>
          <div className="topbar-right">
            <span className="user-chip">{email} · {roles.join(',')}</span>
            <button className="btn ghost" onClick={logout}>Salir</button>
          </div>
        </header>
        <div className="content">
          <Outlet />
        </div>
      </div>
    </div>
  )
}
