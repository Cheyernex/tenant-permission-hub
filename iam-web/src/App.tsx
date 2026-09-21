import { useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import { Layout } from './components/Layout'
import { Dashboard } from './pages/Dashboard'
import { ModulesPage } from './pages/ModulesPage'
import { UsersPage } from './pages/UsersPage'
import { TemplatesPage } from './pages/TemplatesPage'
import { LoginPage } from './pages/LoginPage'
import type { Modulo } from './types/iam'
import { seedModulos } from './api/modules'
import { apiFetch } from './api/client'

function AppInner() {
  const { token } = useAuth()
  const [modulos, setModulos] = useState<Modulo[]>(seedModulos)
  const [selected, setSelected] = useState<{ m: Modulo, sId?: number } | null>(null)

  // Intento de fetch real — cae a seed si no hay backend/auth
  useEffect(() => {
    let cancelled = false
    apiFetch('/api/v1/modulos?page=0&size=100')
      .then((data: any) => {
        // Spring Page: data.content
        const list = Array.isArray(data) ? data : data?.content
        if (!cancelled && Array.isArray(list) && list.length) {
          // Normaliza a Modulo[] con submodulos incluidos si vienen
          setModulos(list.map((m: any) => ({
            ...m,
            submodulos: m.submodulos || [],
          })))
        }
      })
      .catch(() => { /* usa seed */ })
    return () => { cancelled = true }
  }, [token])

  if (!token) return <LoginPage />

  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout modulos={modulos} onSelect={(m, sId) => setSelected({ m, sId })} />}>
          <Route path="/" element={<Dashboard modulos={modulos} />} />
          <Route path="/modulos" element={<ModulesPage modulos={modulos} setModulos={setModulos} />} />
          <Route path="/usuarios" element={<UsersPage modulos={modulos} />} />
          <Route path="/plantillas" element={<TemplatesPage modulos={modulos} />} />
          <Route path="*" element={<Navigate to="/" />} />
        </Route>
      </Routes>
      {selected && (
        <div className="login-wrap" style={{ position:'fixed', inset:0, background:'rgba(0,0,0,.6)' }} onClick={() => setSelected(null)}>
          <div className="login-card" onClick={e=>e.stopPropagation()}>
            <h3>{selected.m.nombre} {selected.sId ? '· ' + selected.m.submodulos.find(s=>s.id===selected.sId)?.nombre : ''}</h3>
            <p className="muted sm">Los permisos son <code>Submódulo:Acción</code> (ej: <code>Productos:CREAR</code>). Este modal demuestra que el sidebar es 100% dinámico: crea un submódulo/acción en Módulos y aparece aquí.</p>
            <button className="btn primary block" onClick={()=>setSelected(null)}>Cerrar</button>
          </div>
        </div>
      )}
    </BrowserRouter>
  )
}

export default function App() {
  return <AuthProvider><AppInner /></AuthProvider>
}
