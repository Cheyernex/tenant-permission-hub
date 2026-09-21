import { useState } from 'react'
import { NavLink } from 'react-router-dom'
import type { Modulo } from '../types/iam'

export function Sidebar({ modulos, onSelect }: { modulos: Modulo[], onSelect?: (m: Modulo, sId?: number) => void }) {
  const [open, setOpen] = useState<Record<number, boolean>>({ 1: true })

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div className="brand-mark">◈</div>
        <div>
          <div className="brand-title">Tenant Hub</div>
          <div className="brand-sub">IAM Modular</div>
        </div>
      </div>

      <nav className="nav-section">
        <div className="nav-label">General</div>
        <NavLink to="/" className="nav-item">▣ Dashboard</NavLink>
        <NavLink to="/modulos" className="nav-item">⬢ Módulos</NavLink>
        <NavLink to="/usuarios" className="nav-item">◎ Usuarios</NavLink>
        <NavLink to="/plantillas" className="nav-item">⬔ Plantillas</NavLink>
      </nav>

      <div className="nav-section">
        <div className="nav-label">Catálogo dinámico <span className="badge">{modulos.length}</span></div>
        {modulos.filter(m => m.activo).map(m => (
          <div key={m.id} className="modulo-group">
            <button className="modulo-head" onClick={() => setOpen(o => ({ ...o, [m.id]: !o[m.id] }))}>
              <span className="chev">{open[m.id] ? '▾' : '▸'}</span>
              <span className="modulo-name">{m.nombre}</span>
              <span className="modulo-count">{m.submodulos.filter(s => s.activo).length}</span>
            </button>
            {open[m.id] && (
              <div className="submodulo-list">
                {m.submodulos.filter(s => s.activo).map(s => (
                  <button key={s.id} className="submodulo-item" onClick={() => onSelect?.(m, s.id)}>
                    <span className="dot" /> {s.nombre}
                    <span className="accion-count">{s.acciones.filter(a => a.activo).length} acc.</span>
                  </button>
                ))}
                <button className="submodulo-add" onClick={() => onSelect?.(m)}>+ Submódulo</button>
              </div>
            )}
          </div>
        ))}
        <div className="sidebar-hint">Los módulos son <b>globales</b> (sin tenant_id). Submódulos/acciones se crean dinámicamente y aparecen aquí al instante.</div>
      </div>

      <div className="sidebar-footer">
        <div className="legend">
          <span className="legend-dot global" /> Global
          <span className="legend-dot tenant" /> Por tenant
        </div>
      </div>
    </aside>
  )
}
