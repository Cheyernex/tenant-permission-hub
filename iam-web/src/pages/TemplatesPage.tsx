import { useState } from 'react'
import type { Modulo } from '../types/iam'

export function TemplatesPage({ modulos }: { modulos: Modulo[] }) {
  const perms = modulos.flatMap(m => m.submodulos.flatMap(s => s.acciones.filter(a => a.activo).map(a => ({ id: s.id * 1000 + a.id, key: `${s.nombre}:${a.nombre}` }))))
  const [plantillas, setPlantillas] = useState<{ id: number, nombre: string, perms: number[] }[]>([
    { id: 1, nombre: 'Operador Catalogo', perms: perms.filter(p => p.key.startsWith('Productos')).slice(0,2).map(p=>p.id) }
  ])
  const [nombre, setNombre] = useState('')
  const [sel, setSel] = useState<number[]>([])

  const crear = () => {
    if (!nombre.trim() || sel.length===0) return
    setPlantillas([...plantillas, { id: Date.now(), nombre: nombre.trim(), perms: [...sel] }])
    setNombre(''); setSel([])
  }

  return (
    <div className="page">
      <h1>Plantillas por tenant</h1>
      <p className="muted">Una plantilla es un conjunto nombrado de <code>Submódulo+Acción</code>. Creación valida <b>anti-escalación</b>: solo permisos que el admin posee (vía <code>PermissionCacheService</code>). Asignación a usuarios valida aislamiento tenant (trigger <code>trg_usuario_plantilla_tenant</code>).</p>
      <div className="toolbar">
        <input placeholder="Nombre plantilla" value={nombre} onChange={e=>setNombre(e.target.value)} />
        <button className="btn primary" onClick={crear}>+ Crear plantilla</button>
      </div>
      <div className="chips">
        {perms.map(p => (
          <label key={p.id} className={`chip check ${sel.includes(p.id) ? 'on' : ''}`}><input type="checkbox" checked={sel.includes(p.id)} onChange={()=>setSel(s=> s.includes(p.id)? s.filter(x=>x!==p.id): [...s,p.id])}/> {p.key}</label>
        ))}
      </div>
      <div className="grid">
        {plantillas.map(pl => (
          <div key={pl.id} className="mod-card">
            <b>{pl.nombre}</b> <span className="badge">{pl.perms.length} perms</span>
            <div className="chips">{pl.perms.map(id => <span key={id} className="chip">{perms.find(p=>p.id===id)?.key}</span>)}</div>
            <div className="muted sm">Edición es <b>retroactiva</b> (afecta a usuarios ya asignados) · Desactivar no borra histórico.</div>
          </div>
        ))}
      </div>
    </div>
  )
}
