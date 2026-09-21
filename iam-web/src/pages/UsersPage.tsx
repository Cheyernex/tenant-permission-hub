import { useEffect, useState } from 'react'
import type { Modulo } from '../types/iam'
import { apiFetch } from '../api/client'

type Usuario = { id:number, email:string, nombre:string, esAdmin:boolean, tenantId:number }

export function UsersPage({ modulos }: { modulos: Modulo[] }) {
  const permisos = modulos.flatMap(m => m.submodulos.flatMap(s => s.acciones.filter(a => a.activo && s.activo && m.activo).map(a => ({
    id: s.id * 1000 + a.id, key: `${s.nombre}:${a.nombre}`, submoduloId: s.id, accionId: a.id
  }))))

  const [users, setUsers] = useState<Usuario[]>([])
  const [selected, setSelected] = useState<number | null>(null)
  const [efectivos, setEfectivos] = useState<string[]>([])
  const [msg, setMsg] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [newUser, setNewUser] = useState({ email:'', nombre:'', password:'password123', esAdmin:false })

  const loadUsers = async () => {
    try {
      const data = await apiFetch('/api/v1/usuarios?page=0&size=100')
      const list = Array.isArray(data) ? data : data?.content || []
      setUsers(list.map((u:any)=> ({ id:u.id, email:u.email, nombre:u.nombre, esAdmin:u.esAdmin, tenantId:u.tenantId })))
      if (list.length && selected===null) setSelected(list[0].id)
    } catch (e:any) { setMsg('Error cargando usuarios (requiere TENANT_ADMIN JWT): '+e.message) }
  }
  useEffect(()=>{ loadUsers() }, [])

  const loadEfectivos = async (uid:number) => {
    try {
      const data = await apiFetch(`/api/v1/usuarios/${uid}/permisos/efectivos`)
      setEfectivos(Array.isArray(data) ? data.map((p:any)=>p.key || `${p.submoduloNombre}:${p.accionNombre}`) : [])
    } catch { setEfectivos([]) }
  }
  useEffect(()=>{ if(selected) loadEfectivos(selected) }, [selected])

  const createUser = async () => {
    if(!newUser.email || !newUser.nombre) return
    setBusy(true)
    try {
      await apiFetch('/api/v1/usuarios', { method:'POST', body: JSON.stringify(newUser) })
      setNewUser({ email:'', nombre:'', password:'password123', esAdmin:false }); await loadUsers(); setMsg('Usuario creado (tenant del JWT).')
    } catch(e:any){ setMsg('Error crear usuario: '+e.message) } finally{ setBusy(false) }
  }

  const toggleDirect = async (permId:number) => {
    if(selected===null) return
    setBusy(true)
    try {
      const isOn = efectivos.includes(permisos.find(p=>p.id===permId)?.key || '')
      if(isOn){
        await apiFetch(`/api/v1/usuarios/${selected}/permisos/${permId}`, { method:'DELETE' })
      } else {
        await apiFetch(`/api/v1/usuarios/${selected}/permisos`, { method:'POST', body: JSON.stringify({ permisoIds:[permId] }) })
      }
      await loadEfectivos(selected)
    } catch(e:any){ setMsg('Error permiso (anti-escalación/aislamiento): '+e.message) } finally{ setBusy(false) }
  }

  return (
    <div className="page">
      <h1>Usuarios — aislamiento por tenant</h1>
      <p className="muted"><code>POST /api/v1/usuarios/{'{id}'}/permisos {'{permisoIds}'}</code> valida <code>tenant_id == JWT</code> + <code>anti-escalación</code> vía <code>PermissionValidationService</code> + <code>SET LOCAL app.current_admin_id</code> trigger.</p>
      {msg && <div className="callout">{msg}</div>}
      <div className="toolbar">
        <input placeholder="email" value={newUser.email} onChange={e=>setNewUser({...newUser,email:e.target.value})} />
        <input placeholder="nombre" value={newUser.nombre} onChange={e=>setNewUser({...newUser,nombre:e.target.value})} />
        <label className="check-row"><input type="checkbox" checked={newUser.esAdmin} onChange={e=>setNewUser({...newUser,esAdmin:e.target.checked})} /> admin</label>
        <button className="btn primary" onClick={createUser} disabled={busy}>+ Usuario</button>
        <button className="btn" onClick={loadUsers}>↻ Recargar</button>
      </div>
      <div className="split">
        <div className="panel">
          <h3>Usuarios (tenant del JWT)</h3>
          {users.map(u => (
            <button key={u.id} className={`list-item ${selected===u.id?'active':''}`} onClick={()=>setSelected(u.id)}>
              {u.nombre} <span className="muted">{u.email} {u.esAdmin?'· admin':''}</span>
            </button>
          ))}
          {users.length===0 && <div className="muted">Sin usuarios — crea uno arriba.</div>}
        </div>
        <div className="panel">
          <h3>Efectivos {selected ? `· usuario ${selected}` : ''}</h3>
          <div className="chips">
            {permisos.map(p => (
              <label key={p.id} className={`chip check ${efectivos.includes(p.key)?'on':''}`}>
                <input type="checkbox" checked={efectivos.includes(p.key)} onChange={()=>toggleDirect(p.id)} disabled={busy} /> {p.key}
              </label>
            ))}
          </div>
          <div className="callout"><b>Efectivos (UNION directos+plantillas, cache Redis):</b> {efectivos.join(', ') || '—'}</div>
        </div>
      </div>
    </div>
  )
}
