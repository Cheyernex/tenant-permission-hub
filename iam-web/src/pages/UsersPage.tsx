import { useState } from 'react'
import type { Modulo, Permiso } from '../types/iam'

export function UsersPage({ modulos }: { modulos: Modulo[] }) {
  const permisos: Permiso[] = modulos.flatMap(m => m.submodulos.flatMap(s => s.acciones.filter(a => a.activo && s.activo && m.activo).map(a => ({
    id: s.id * 1000 + a.id, key: `${s.nombre}:${a.nombre}`, submoduloId: s.id, submoduloNombre: s.nombre, accionId: a.id, accionNombre: a.nombre
  }))))

  const [users] = useState([
    { id: 1, email: 'admin@demo.local', nombre: 'Admin Demo', tenant: 'demo', esAdmin: true },
    { id: 2, email: 'ana@demo.local', nombre: 'Ana', tenant: 'demo', esAdmin: false },
  ])
  const [selected, setSelected] = useState<number>(2)
  const [direct, setDirect] = useState<Record<number, number[]>>({ 2: [] })
  const [plantilla, setPlantilla] = useState<string[]>([])

  const toggleDirect = (pid: number) => {
    setDirect(d => {
      const cur = d[selected] || []
      return { ...d, [selected]: cur.includes(pid) ? cur.filter(x => x !== pid) : [...cur, pid] }
    })
  }

  const efectivos = new Set([...(direct[selected] || []), ...plantilla.flatMap(name => {
    if (name === 'Operador Catalogo') return permisos.filter(p => p.submoduloNombre === 'Productos' && ['VER','EDITAR'].includes(p.accionNombre)).map(p => p.id)
    return []
  })])

  return (
    <div className="page">
      <h1>Usuarios (por tenant — aislamiento)</h1>
      <p className="muted">Un TENANT_ADMIN solo ve usuarios de su tenant. Asignación directa valida <code>anti-escalación</code> (solo permisos que el admin posee) + <code>SET LOCAL app.current_admin_id</code> trigger.</p>
      <div className="split">
        <div className="panel">
          <h3>Usuarios tenant demo</h3>
          {users.map(u => (
            <button key={u.id} className={`list-item ${selected === u.id ? 'active' : ''}`} onClick={() => setSelected(u.id)}>
              {u.nombre} <span className="muted">{u.email} {u.esAdmin ? '· admin' : ''}</span>
            </button>
          ))}
        </div>
        <div className="panel">
          <h3>Permisos directos → {users.find(u => u.id === selected)?.nombre}</h3>
          <div className="chips">
            {permisos.map(p => (
              <label key={p.id} className={`chip check ${efectivos.has(p.id) ? 'on' : ''}`}>
                <input type="checkbox" checked={(direct[selected] || []).includes(p.id)} onChange={() => toggleDirect(p.id)} /> {p.key}
              </label>
            ))}
          </div>
          <h4>Plantillas asignadas</h4>
          <label className="check-row"><input type="checkbox" checked={plantilla.includes('Operador Catalogo')} onChange={e => setPlantilla(e.target.checked ? ['Operador Catalogo'] : [])} /> Operador Catalogo (Productos:VER, EDITAR)</label>
          <div className="callout">
            <b>Efectivos (UNION directos + plantilla, DISTINCT):</b> {[...efectivos].length} — {[...efectivos].map(id => permisos.find(p => p.id === id)?.key).join(', ') || '—'}
          </div>
          <button className="btn primary" onClick={() => alert('En integración real: POST /api/v1/usuarios/' + selected + '/permisos {permisoIds} con JWT tenant_id')}>Guardar (mock)</button>
        </div>
      </div>
    </div>
  )
}
