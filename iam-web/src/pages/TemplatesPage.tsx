import { useEffect, useState } from 'react'
import type { Modulo } from '../types/iam'
import { apiFetch } from '../api/client'

export function TemplatesPage({ modulos, onReload }: { modulos: Modulo[], onReload: () => void }) {
  const perms = modulos.flatMap(m => m.submodulos.flatMap(s => s.acciones.filter(a => a.activo).map(a => ({ id: s.id * 1000 + a.id, key: `${s.nombre}:${a.nombre}` }))))
  const [plantillas, setPlantillas] = useState<any[]>([])
  const [nombre, setNombre] = useState('')
  const [sel, setSel] = useState<number[]>([])
  const [msg, setMsg] = useState<string|null>(null)
  const [busy, setBusy] = useState(false)

  const load = async () => {
    try {
      const data = await apiFetch('/api/v1/plantillas?page=0&size=100')
      const list = Array.isArray(data) ? data : data?.content || []
      setPlantillas(list)
    } catch(e:any){ setMsg('Error plantillas: '+e.message) }
  }
  useEffect(()=>{ load() }, [])

  const crear = async () => {
    if (!nombre.trim() || sel.length===0) return
    setBusy(true)
    try {
      await apiFetch('/api/v1/plantillas', { method:'POST', body: JSON.stringify({ nombre: nombre.trim(), permisoIds: sel }) })
      setNombre(''); setSel([]); await load(); onReload(); setMsg('Plantilla creada (validada anti-escalación).')
    } catch(e:any){ setMsg('Error crear plantilla (solo permisos que posees): '+e.message) } finally{ setBusy(false) }
  }

  return (
    <div className="page">
      <h1>Plantillas por tenant</h1>
      <p className="muted"><code>POST /api/v1/plantillas {'{nombre, permisoIds}'}</code> valida <b>anti-escalación</b> + <code>SET LOCAL app.current_admin_id</code>. Asignación <code>POST /usuarios/{'{id}'}/plantillas/{'{plantillaId}'}</code> valida <code>trg_usuario_plantilla_tenant</code> + evict <code>eff_perms</code>.</p>
      {msg && <div className="callout">{msg}</div>}
      <div className="toolbar">
        <input placeholder="Nombre plantilla" value={nombre} onChange={e=>setNombre(e.target.value)} disabled={busy} />
        <button className="btn primary" onClick={crear} disabled={busy}>+ Crear plantilla</button>
        <button className="btn" onClick={load}>↻ Recargar</button>
      </div>
      <div className="chips">
        {perms.map(p => (
          <label key={p.id} className={`chip check ${sel.includes(p.id) ? 'on' : ''}`}><input type="checkbox" checked={sel.includes(p.id)} onChange={()=>setSel(s=> s.includes(p.id)? s.filter(x=>x!==p.id): [...s,p.id])} disabled={busy}/> {p.key}</label>
        ))}
      </div>
      <div className="grid">
        {plantillas.map((pl:any) => (
          <div key={pl.id} className="mod-card">
            <b>{pl.nombre}</b> <span className="badge">{pl.permisos?.length || 0} perms</span>
            <div className="chips">{(pl.permisos||[]).map((p:any)=><span key={p.id} className="chip">{p.key || `${p.submoduloNombre}:${p.accionNombre}`}</span>)}</div>
            <div className="muted sm">Edición retroactiva · Desactivar no borra histórico.</div>
          </div>
        ))}
      </div>
      {plantillas.length===0 && <div className="callout">Sin plantillas — crea una seleccionando permisos arriba.</div>}
    </div>
  )
}
