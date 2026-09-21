import { useState } from 'react'
import { NavLink } from 'react-router-dom'
import type { Modulo } from '../types/iam'
import {
  IconDashboard,
  IconModules,
  IconUsers,
  IconTemplate,
  IconChevronDown,
  IconChevronRight,
  IconPlus,
  IconShield,
  IconFolder
} from './Icons'

export function Sidebar({ modulos, onSelect }: { modulos: Modulo[], onSelect?: (m: Modulo, sId?: number) => void }) {
  const [open, setOpen] = useState<Record<number, boolean>>({ 1: true, 2: true, 3: true })

  return (
    <aside className="sidebar">
      {/* Brand Header */}
      <div className="sidebar-brand">
        <div className="brand-mark">
          <IconShield size={22} color="#ffffff" />
        </div>
        <div>
          <div className="brand-title">Tenant Hub</div>
          <div className="brand-sub">
            IAM Modular <span className="brand-version">v2.0</span>
          </div>
        </div>
      </div>

      <div className="sidebar-scroll">
        {/* Main Navigation */}
        <nav className="nav-section">
          <div className="nav-label">Navegación</div>
          <NavLink to="/" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <span className="nav-item-icon"><IconDashboard size={18} /></span>
            Dashboard
          </NavLink>
          <NavLink to="/modulos" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <span className="nav-item-icon"><IconModules size={18} /></span>
            Módulos Globales
          </NavLink>
          <NavLink to="/usuarios" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <span className="nav-item-icon"><IconUsers size={18} /></span>
            Usuarios & Permisos
          </NavLink>
          <NavLink to="/plantillas" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <span className="nav-item-icon"><IconTemplate size={18} /></span>
            Plantillas por Tenant
          </NavLink>
          <NavLink to="/catalogo" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`} style={{ border: '1px dashed #4f46e5' }}>
            <span className="nav-item-icon">◈</span>
            Catálogo por Tenant <span style={{fontSize:'10px', background:'#4f46e5', color:'white', padding:'1px 5px', borderRadius:999}}>SUPER</span>
          </NavLink>
        </nav>

        {/* Dynamic Catalog Navigation */}
        <div className="nav-section">
          <div className="nav-label">
            <span>Catálogo Dinámico</span>
            <span className="badge" style={{ fontSize: '10px' }}>{modulos.filter(m => m.activo).length}</span>
          </div>

          {modulos.filter(m => m.activo).map(m => {
            const isOpen = open[m.id] ?? false
            const activeSubCount = m.submodulos.filter(s => s.activo).length

            return (
              <div key={m.id} className="modulo-group">
                <button
                  className="modulo-head"
                  onClick={() => setOpen(o => ({ ...o, [m.id]: !isOpen }))}
                >
                  <span className="chev">
                    {isOpen ? <IconChevronDown size={14} /> : <IconChevronRight size={14} />}
                  </span>
                  <IconFolder size={15} color="#818cf8" style={{ marginRight: '2px' }} />
                  <span className="modulo-name">{m.nombre}</span>
                  <span className="modulo-count">{activeSubCount}</span>
                </button>

                {isOpen && (
                  <div className="submodulo-list">
                    {m.submodulos.filter(s => s.activo).map(s => (
                      <button
                        key={s.id}
                        className="submodulo-item"
                        onClick={() => onSelect?.(m, s.id)}
                      >
                        <span className="dot" />
                        <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          {s.nombre}
                        </span>
                        <span className="accion-count">
                          {s.acciones.filter(a => a.activo).length} acc.
                        </span>
                      </button>
                    ))}

                    <button
                      className="submodulo-add"
                      onClick={() => onSelect?.(m)}
                    >
                      <IconPlus size={13} />
                      Nuevo Submódulo
                    </button>
                  </div>
                )}
              </div>
            )
          })}

          <div className="sidebar-hint" style={{ marginTop: '8px' }}>
            <b>SUPER_ADMIN</b> crea el catálogo global y lo <b>asigna</b> a tenants en <code>/catalogo</code>. <b>TENANT_ADMIN</b> solo ve lo asignado y crea plantillas con ello.
          </div>
        </div>
      </div>

      {/* Footer Legend */}
      <div className="sidebar-footer">
        <div className="legend">
          <div className="legend-item">
            <span className="legend-dot" /> Global
          </div>
          <div className="legend-item">
            <span className="legend-dot tenant" /> Por Tenant
          </div>
        </div>
      </div>
    </aside>
  )
}
