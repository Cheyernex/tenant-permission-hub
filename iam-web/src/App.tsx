import { useCallback, useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import { Layout } from './components/Layout'
import { Dashboard } from './pages/Dashboard'
import { ModulesPage } from './pages/ModulesPage'
import { UsersPage } from './pages/UsersPage'
import { TemplatesPage } from './pages/TemplatesPage'
import { LoginPage } from './pages/LoginPage'
import type { Modulo } from './types/iam'
import { fetchModulos } from './api/modules'

function AppInner() {
  const { token } = useAuth()
  const [modulos, setModulos] = useState<Modulo[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [selected, setSelected] = useState<{ m: Modulo, sId?: number } | null>(null)

  const reload = useCallback(async () => {
    if (!token) return
    setLoading(true); setError(null)
    try {
      const list = await fetchModulos()
      setModulos(list)
    } catch (e: any) {
      setError(e.message || 'Error cargando módulos')
    } finally { setLoading(false) }
  }, [token])

  useEffect(() => { reload() }, [reload])

  if (!token) return <LoginPage />

  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout modulos={modulos} onSelect={(m, sId) => setSelected({ m, sId })} />}>
          <Route path="/" element={<><Dashboard modulos={modulos} />{error && <div className="callout" style={{borderColor:'#f87171'}}><b>Error:</b> {error} — Verifica <code>docker-compose up -d</code> y login con tenant existente. <button className="btn sm" onClick={reload}>Reintentar</button></div>}{loading && <div className="muted">Cargando módulos desde PostgreSQL…</div>}</>} />
          <Route path="/modulos" element={<ModulesPage modulos={modulos} onReload={reload} />} />
          <Route path="/usuarios" element={<UsersPage modulos={modulos} />} />
          <Route path="/plantillas" element={<TemplatesPage modulos={modulos} onReload={reload} />} />
          <Route path="*" element={<Navigate to="/" />} />
        </Route>
      </Routes>
      {selected && (
        <div className="login-wrap" style={{ position:'fixed', inset:0, background:'rgba(0,0,0,.6)' }} onClick={() => setSelected(null)}>
          <div className="login-card" onClick={e=>e.stopPropagation()}>
            <h3>{selected.m.nombre} {selected.sId ? '· ' + selected.m.submodulos.find(s=>s.id===selected.sId)?.nombre : ''}</h3>
            <p className="muted sm">Los permisos son <code>Submódulo:Acción</code> persistidos en <code>permiso(submodulo_id,accion_id)</code>. Este modal confirma que el sidebar se genera 100% desde <code>GET /api/v1/modulos</code> (PostgreSQL).</p>
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
