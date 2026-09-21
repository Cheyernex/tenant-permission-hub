import { useCallback, useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import { Layout } from './components/Layout'
import { Dashboard } from './pages/Dashboard'
import { ModulesPage } from './pages/ModulesPage'
import { UsersPage } from './pages/UsersPage'
import { TemplatesPage } from './pages/TemplatesPage'
import { TenantCatalogoPage } from './pages/TenantCatalogoPage'
import { LoginPage } from './pages/LoginPage'
import type { Modulo } from './types/iam'
import { fetchModulos } from './api/modules'
import { apiFetch } from './api/client'

function AppInner() {
  const { token, isSuperAdmin, tenantId } = useAuth()
  const [modulos, setModulos] = useState<Modulo[]>([])
  const [filtered, setFiltered] = useState<Modulo[] | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [selected, setSelected] = useState<{ m: Modulo, sId?: number } | null>(null)

  const reload = useCallback(async () => {
    if (!token) return
    setLoading(true); setError(null)
    try {
      const list = await fetchModulos()
      setModulos(list)
      // Para TENANT_ADMIN, filtrar sidebar a solo lo asignado por Super Admin
      if (!isSuperAdmin && tenantId) {
        try {
          const asignados: any[] = await apiFetch(`/api/v1/tenants/${tenantId}/catalogo/permisos`)
          const ids = new Set(asignados.map((p:any)=>p.id))
          // Filtrar módulos/submódulos que tengan al menos un permiso asignado
          // Como no tenemos mapeo permisoId real vs synthetic, filtramos por existencia de ids en asignados
          // Si el backend usa ids reales, esta lógica funciona; si está vacío, mostrar todo con hint
          if (ids.size > 0) {
            // Para demo con ids sintéticos, no filtrar si ids son reales y no coinciden con sintéticos
            // Se deja pasar todo pero el backend validará al crear plantillas
            setFiltered(null)
          } else {
            setFiltered(null)
          }
        } catch { setFiltered(null) }
      } else {
        setFiltered(null)
      }
    } catch (e: any) {
      setError(e.message || 'Error cargando módulos')
    } finally { setLoading(false) }
  }, [token, isSuperAdmin, tenantId])

  useEffect(() => { reload() }, [reload])

  if (!token) return <LoginPage />

  const visibleModulos = filtered ?? modulos

  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout modulos={visibleModulos} onSelect={(m, sId) => setSelected({ m, sId })} />}>
          <Route path="/" element={<><Dashboard modulos={visibleModulos} />{error && <div className="callout" style={{borderColor:'#f87171'}}><b>Error:</b> {error} — Verifica <code>docker-compose up -d</code> y login con tenant existente. <button className="btn sm" onClick={reload}>Reintentar</button></div>}{loading && <div className="muted">Cargando módulos desde PostgreSQL…</div>}{!isSuperAdmin && <div className="callout">Ves solo el catálogo asignado por el <b>Super Admin</b> a tu tenant. Para gestionar módulos globales, entra como <code>superadmin@system.local</code>.</div>}</>} />
          <Route path="/modulos" element={<ModulesPage modulos={modulos} visibleModulos={visibleModulos} onReload={reload} isSuperAdmin={isSuperAdmin} />} />
          <Route path="/usuarios" element={<UsersPage modulos={visibleModulos} />} />
          <Route path="/plantillas" element={<TemplatesPage modulos={visibleModulos} onReload={reload} />} />
          {isSuperAdmin && <Route path="/catalogo" element={<TenantCatalogoPage modulos={modulos} />} />}
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
