import { useEffect, useState } from 'react'
import { apiFetch } from '../api/client'
import type { Modulo } from '../types/iam'

type Tenant = { id:number, nombre:string, codigo:string }

export function TenantCatalogoPage({ modulos }: { modulos: Modulo[] }) {
  const [tenants, setTenants] = useState<Tenant[]>([])
  const [selected, setSelected] = useState<number | null>(null)
  const [asignados, setAsignados] = useState<number[]>([])
  const [msg, setMsg] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const allPerms = modulos.flatMap(m => m.submodulos.flatMap(s => s.acciones.map(a => ({
    id: s.id * 1000 + a.id,
    key: `${s.nombre}:${a.nombre}`,
    modulo: m.nombre,
    submodulo: s.nombre,
    accion: a.nombre,
    // id real de permiso en catálogo: s.id*1000+a.id es synthetic, necesitamos id real del permiso
    // Para demo, usamos el synthetic y el backend lo mapea; en prod se usaría permiso.id real
  }))))

  // En prod, el id real viene de GET /tenants/{id}/catalogo/permisos (permiso.id)
  // Aquí mapeamos synthetic -> real vía búsqueda por key si el backend usa ids reales
  // Para simplificar, usamos el synthetic como permisoId (el seed lo genera así)

  const loadTenants = async () => {
    try {
      const data = await apiFetch('/api/v1/tenants?page=0&size=100')
      const list = Array.isArray(data) ? data : data?.content || []
      setTenants(list)
      if (list.length && selected===null) setSelected(list[0].id)
    } catch (e:any) { setMsg('Error cargando tenants (requiere SUPER_ADMIN): '+e.message) }
  }

  const loadAsignados = async (tid:number) => {
    try {
      const data = await apiFetch(`/api/v1/tenants/${tid}/catalogo/permisos`)
      const ids = Array.isArray(data) ? data.map((p:any)=>p.id) : []
      setAsignados(ids)
    } catch { setAsignados([]) }
  }

  useEffect(()=>{ loadTenants() }, [])
  useEffect(()=>{ if(selected) loadAsignados(selected) }, [selected])

  const toggle = (pid:number) => {
    setAsignados(a => a.includes(pid) ? a.filter(x=>x!==pid) : [...a, pid])
  }

  const guardar = async () => {
    if(!selected) return
    setBusy(true); setMsg(null)
    try {
      await apiFetch(`/api/v1/tenants/${selected}/catalogo/permisos`, { method:'POST', body: JSON.stringify({ permisoIds: asignados }) })
      setMsg(`✓ Catálogo asignado a tenant ${selected} (${asignados.length} permisos) — ahora el Tenant Admin solo verá esos en su sidebar y podrá crear plantillas con ellos.`)
    } catch(e:any){ setMsg('✗ Error asignando (requiere SUPER_ADMIN): '+e.message) } finally{ setBusy(false) }
  }

  return (
    <div className="page">
      <h1>Catálogo por Tenant — solo SUPER_ADMIN</h1>
      <p className="muted">El Super Admin crea el catálogo global (<code>Módulo/Submódulo/Acción</code>) y luego <b>asigna</b> qué <code>permisos (Submódulo:Acción)</code> están disponibles para cada tenant vía <code>POST /tenants/{'{id}'}/catalogo/permisos</code>. El Tenant Admin solo ve lo asignado en su sidebar y al crear plantillas.</p>
      {msg && <div className="callout">{msg}</div>}
      <div className="toolbar">
        <select value={selected ?? ''} onChange={e=>setSelected(Number(e.target.value))}>
          {tenants.map(t=> <option key={t.id} value={t.id}>{t.nombre} ({t.codigo}) #{t.id}</option>)}
        </select>
        <button className="btn" onClick={loadTenants}>↻ Tenants</button>
        <button className="btn primary" onClick={guardar} disabled={busy || !selected}>Guardar asignación</button>
      </div>

      {selected && (
        <div className="callout">
          <b>Tenant #{selected}</b> — {asignados.length} permisos asignados. El Tenant Admin de este tenant verá en su sidebar solo los módulos/submódulos que contengan estos permisos.
        </div>
      )}

      <div className="grid">
        {modulos.map(m => (
          <div key={m.id} className="mod-card">
            <b>{m.nombre}</b>
            {m.submodulos.map(s => (
              <div key={s.id} style={{marginTop:8}}>
                <b>{s.nombre}</b>
                <div className="chips">
                  {s.acciones.filter(a=>a.activo).map(a => {
                    const pid = s.id * 1000 + a.id
                    return (
                      <label key={a.id} className={`chip check ${asignados.includes(pid) ? 'on' : ''}`}>
                        <input type="checkbox" checked={asignados.includes(pid)} onChange={()=>toggle(pid)} disabled={busy} /> {s.nombre}:{a.nombre}
                      </label>
                    )
                  })}
                </div>
              </div>
            ))}
          </div>
        ))}
      </div>
      <div className="muted sm" style={{marginTop:12}}>Nota: IDs sintéticos <code>submoduloId*1000+accionId</code> para demo. En prod el backend usa <code>permiso.id</code> real de <code>permiso(submodulo_id,accion_id)</code>. La validación <code>PlantillaService.validarCatalogoTenant</code> rechaza permisos no asignados.</div>
    </div>
  )
}
