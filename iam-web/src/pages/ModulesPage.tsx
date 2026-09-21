import { useState } from 'react'
import type { Modulo } from '../types/iam'

export function ModulesPage({ modulos, setModulos }: { modulos: Modulo[], setModulos: (m: Modulo[]) => void }) {
  const [nombre, setNombre] = useState('')
  const [desc, setDesc] = useState('')
  const [subForm, setSubForm] = useState<{ moduloId: number | null, nombre: string, acc: string }>({ moduloId: null, nombre: '', acc: '' })
  const [accionForm, setAccionForm] = useState<{ subId: number | null, nombre: string }>({ subId: null, nombre: '' })

  const addModulo = () => {
    if (!nombre.trim()) return
    const id = Date.now()
    setModulos([...modulos, { id, nombre: nombre.trim(), descripcion: desc, orden: modulos.length + 1, activo: true, submodulos: [] }])
    setNombre(''); setDesc('')
  }

  const addSubmodulo = () => {
    if (!subForm.moduloId || !subForm.nombre.trim()) return
    const accs = subForm.acc.split(',').map(s => s.trim()).filter(Boolean)
    setModulos(modulos.map(m => m.id === subForm.moduloId ? {
      ...m, submodulos: [...m.submodulos, {
        id: Date.now(), nombre: subForm.nombre.trim(), descripcion: '', orden: m.submodulos.length + 1, activo: true,
        acciones: accs.map((a, i) => ({ id: Date.now() + i + 1, nombre: a.toUpperCase(), activo: true }))
      }]
    } : m))
    setSubForm({ moduloId: null, nombre: '', acc: '' })
  }

  const addAccion = () => {
    if (!accionForm.subId || !accionForm.nombre.trim()) return
    setModulos(modulos.map(m => ({
      ...m, submodulos: m.submodulos.map(s => s.id === accionForm.subId ? {
        ...s, acciones: [...s.acciones, { id: Date.now(), nombre: accionForm.nombre.trim().toUpperCase(), activo: true }]
      } : s)
    })))
    setAccionForm({ subId: null, nombre: '' })
  }

  const toggleActivo = (moduloId: number, subId?: number, accId?: number) => {
    setModulos(modulos.map(m => {
      if (m.id !== moduloId) return m
      if (subId === undefined) return { ...m, activo: !m.activo }
      return {
        ...m, submodulos: m.submodulos.map(s => {
          if (s.id !== subId) return s
          if (accId === undefined) return { ...s, activo: !s.activo }
          return { ...s, acciones: s.acciones.map(a => a.id === accId ? { ...a, activo: !a.activo } : a) }
        })
      }
    }))
  }

  return (
    <div className="page">
      <h1>Módulos (global — SUPER_ADMIN escribe, TENANT_ADMIN lee)</h1>
      <div className="toolbar">
        <input placeholder="Nombre módulo (ej: Reportes)" value={nombre} onChange={e => setNombre(e.target.value)} />
        <input placeholder="Descripción" value={desc} onChange={e => setDesc(e.target.value)} />
        <button className="btn primary" onClick={addModulo}>+ Módulo</button>
      </div>

      <div className="grid">
        {modulos.map(m => (
          <div key={m.id} className={`mod-card ${!m.activo ? 'inactive' : ''}`}>
            <div className="mod-card-head">
              <b>{m.nombre}</b> <span className="muted">#{m.id} · orden {m.orden}</span>
              <button className="btn sm" onClick={() => toggleActivo(m.id)}>{m.activo ? 'Desactivar' : 'Activar'}</button>
            </div>
            <div className="muted sm">{m.descripcion || 'Sin descripción'} — {m.activo ? 'activo' : 'inactivo (soft delete)'} </div>

            <div className="sub-grid">
              {m.submodulos.map(s => (
                <div key={s.id} className={`sub-card ${!s.activo ? 'inactive' : ''}`}>
                  <div className="sub-head">
                    <b>{s.nombre}</b> <span className="badge">{s.acciones.length} acc.</span>
                    <button className="btn sm ghost" onClick={() => toggleActivo(m.id, s.id)}>{s.activo ? 'off' : 'on'}</button>
                  </div>
                  <div className="chips">
                    {s.acciones.map(a => (
                      <span key={a.id} className={`chip ${!a.activo ? 'off' : ''}`} onClick={() => toggleActivo(m.id, s.id, a.id)}>
                        {a.nombre}
                      </span>
                    ))}
                  </div>
                  <div className="inline-form">
                    <input placeholder="Nueva acción (VER, EXPORTAR...)" value={accionForm.subId === s.id ? accionForm.nombre : ''} onFocus={() => setAccionForm({ subId: s.id, nombre: '' })} onChange={e => setAccionForm({ subId: s.id, nombre: e.target.value })} />
                    <button className="btn sm" onClick={addAccion} disabled={accionForm.subId !== s.id}>+ Acción</button>
                  </div>
                </div>
              ))}
            </div>

            <div className="inline-form">
              <input placeholder="Submódulo (ej: Dashboard)" value={subForm.moduloId === m.id ? subForm.nombre : ''} onFocus={() => setSubForm({ ...subForm, moduloId: m.id })} onChange={e => setSubForm({ ...subForm, nombre: e.target.value, moduloId: m.id })} />
              <input placeholder="Acciones coma (CREAR,VER,EDITAR)" value={subForm.moduloId === m.id ? subForm.acc : ''} onChange={e => setSubForm({ ...subForm, acc: e.target.value })} />
              <button className="btn sm primary" onClick={addSubmodulo} disabled={subForm.moduloId !== m.id}>+ Submódulo</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
