import { useState } from 'react'
import type { Modulo } from '../types/iam'
import { createAccion, createModulo, createSubmodulo } from '../api/modules'
import { apiFetch } from '../api/client'

export function ModulesPage({ modulos, onReload }: { modulos: Modulo[], onReload: () => void }) {
  const [nombre, setNombre] = useState('')
  const [desc, setDesc] = useState('')
  const [subForm, setSubForm] = useState<{ moduloId: number | null, nombre: string, acc: string }>({ moduloId: null, nombre: '', acc: '' })
  const [accionForm, setAccionForm] = useState<{ subId: number | null, nombre: string }>({ subId: null, nombre: '' })
  const [busy, setBusy] = useState(false)
  const [msg, setMsg] = useState<string | null>(null)

  const handleAddModulo = async () => {
    if (!nombre.trim()) return
    setBusy(true); setMsg(null)
    try {
      await createModulo({ nombre: nombre.trim(), descripcion: desc, orden: modulos.length + 1 })
      setNombre(''); setDesc(''); await onReload(); setMsg('Módulo creado y persistido en PostgreSQL.')
    } catch (e: any) { setMsg('Error: ' + e.message) } finally { setBusy(false) }
  }

  const handleAddSubmodulo = async () => {
    if (!subForm.moduloId || !subForm.nombre.trim()) return
    setBusy(true); setMsg(null)
    try {
      const created = await createSubmodulo(subForm.moduloId, { nombre: subForm.nombre.trim(), orden: 0 }) as any
      // crear acciones iniciales si se indicaron (coma separada)
      const accs = subForm.acc.split(',').map(s => s.trim()).filter(Boolean)
      for (const a of accs) {
        await createAccion(created.id, { nombre: a.toUpperCase() })
      }
      setSubForm({ moduloId: null, nombre: '', acc: '' }); await onReload(); setMsg('Submódulo creado.')
    } catch (e: any) { setMsg('Error submodulo: ' + e.message) } finally { setBusy(false) }
  }

  const handleAddAccion = async (subId: number) => {
    if (!accionForm.nombre.trim()) return
    setBusy(true)
    try {
      await createAccion(subId, { nombre: accionForm.nombre.trim().toUpperCase() })
      setAccionForm({ subId: null, nombre: '' }); await onReload()
    } catch (e: any) { setMsg('Error acción: ' + e.message) } finally { setBusy(false) }
  }

  const handleToggle = async (moduloId: number, subId?: number, accId?: number) => {
    setBusy(true)
    try {
      if (accId !== undefined && subId !== undefined) {
        // No hay endpoint toggle para accion individual en Fase 1, se usa PATCH desactivar/activar
        // Para demo, llamamos al endpoint genérico y luego reload
        const s = modulos.find(m=>m.id===moduloId)?.submodulos.find(x=>x.id===subId)
        const a = s?.acciones.find(x=>x.id===accId)
        const path = a?.activo ? `/api/v1/acciones/${accId}/desactivar` : `/api/v1/acciones/${accId}/activar`
        // Nota: endpoint real es PATCH /acciones/{id}/desactivar (requiere SUPER_ADMIN)
        await apiFetch(path, { method: 'PATCH' })
      } else if (subId !== undefined) {
        const s = modulos.find(m=>m.id===moduloId)?.submodulos.find(x=>x.id===subId)
        const path = s?.activo ? `/api/v1/submodulos/${subId}/desactivar` : `/api/v1/submodulos/${subId}/activar`
        await apiFetch(path, { method: 'PATCH' })
      } else {
        const m = modulos.find(x=>x.id===moduloId)
        const path = m?.activo ? `/api/v1/modulos/${moduloId}/desactivar` : `/api/v1/modulos/${moduloId}/activar`
        await apiFetch(path, { method: 'PATCH' })
      }
      await onReload()
    } catch (e: any) { setMsg('Error toggle (requiere SUPER_ADMIN JWT): ' + e.message) } finally { setBusy(false) }
  }

  return (
    <div className="page">
      <h1>Módulos — persistencia real PostgreSQL (SUPER_ADMIN escribe)</h1>
      <p className="muted">Cada creación hace <code>POST /api/v1/modulos</code> / <code>/submodulos</code> / <code>/acciones</code> con <code>Authorization: Bearer &lt;JWT tenant_id&gt;</code>. Sin mocks.</p>
      {msg && <div className="callout">{msg}</div>}
      <div className="toolbar">
        <input placeholder="Nombre módulo (ej: Reportes)" value={nombre} onChange={e => setNombre(e.target.value)} disabled={busy} />
        <input placeholder="Descripción" value={desc} onChange={e => setDesc(e.target.value)} disabled={busy} />
        <button className="btn primary" onClick={handleAddModulo} disabled={busy}>+ Módulo</button>
      </div>

      <div className="grid">
        {modulos.map(m => (
          <div key={m.id} className={`mod-card ${!m.activo ? 'inactive' : ''}`}>
            <div className="mod-card-head">
              <b>{m.nombre}</b> <span className="muted">#{m.id}</span>
              <button className="btn sm" onClick={() => handleToggle(m.id)} disabled={busy}>{m.activo ? 'Desactivar' : 'Activar'}</button>
            </div>
            <div className="muted sm">{m.descripcion || '—'} — {m.activo ? 'activo' : 'inactivo (soft delete V1)'} </div>

            <div className="sub-grid">
              {m.submodulos.map(s => (
                <div key={s.id} className={`sub-card ${!s.activo ? 'inactive' : ''}`}>
                  <div className="sub-head">
                    <b>{s.nombre}</b> <span className="badge">{s.acciones.length} acc.</span>
                    <button className="btn sm ghost" onClick={() => handleToggle(m.id, s.id)} disabled={busy}>{s.activo ? 'off' : 'on'}</button>
                  </div>
                  <div className="chips">
                    {s.acciones.map(a => (
                      <span key={a.id} className={`chip ${!a.activo ? 'off' : ''}`} onClick={() => handleToggle(m.id, s.id, a.id)} title="Click para toggle (SUPER_ADMIN)">
                        {a.nombre}
                      </span>
                    ))}
                  </div>
                  <div className="inline-form">
                    <input placeholder="Nueva acción (VER...)" value={accionForm.subId === s.id ? accionForm.nombre : ''} onFocus={() => setAccionForm({ subId: s.id, nombre: '' })} onChange={e => setAccionForm({ subId: s.id, nombre: e.target.value })} disabled={busy} />
                    <button className="btn sm" onClick={() => handleAddAccion(s.id)} disabled={busy || accionForm.subId !== s.id}>+ Acción</button>
                  </div>
                </div>
              ))}
            </div>

            <div className="inline-form">
              <input placeholder="Submódulo (ej: Dashboard)" value={subForm.moduloId === m.id ? subForm.nombre : ''} onFocus={() => setSubForm({ ...subForm, moduloId: m.id })} onChange={e => setSubForm({ ...subForm, nombre: e.target.value, moduloId: m.id })} disabled={busy} />
              <input placeholder="Acciones coma (CREAR,VER)" value={subForm.moduloId === m.id ? subForm.acc : ''} onChange={e => setSubForm({ ...subForm, acc: e.target.value })} disabled={busy} />
              <button className="btn sm primary" onClick={handleAddSubmodulo} disabled={busy || subForm.moduloId !== m.id}>+ Submódulo</button>
            </div>
          </div>
        ))}
      </div>
      {modulos.length === 0 && <div className="callout">Sin módulos. Crea uno — quedará persistido y visible para todos los tenants (global).</div>}
    </div>
  )
}
