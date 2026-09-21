import { Link } from 'react-router-dom'
import type { Modulo } from '../types/iam'
import {
  IconDashboard,
  IconModules,
  IconFolder,
  IconZap,
  IconShield,
  IconDatabase,
  IconServer,
  IconKey,
  IconUsers,
  IconTemplate,
  IconSparkles
} from '../components/Icons'

export function Dashboard({ modulos }: { modulos: Modulo[] }) {
  const totalSub = modulos.reduce((a, m) => a + m.submodulos.length, 0)
  const totalAcc = modulos.reduce((a, m) => a + m.submodulos.reduce((s, sub) => s + sub.acciones.length, 0), 0)

  return (
    <div className="page">
      {/* Hero Welcome Header */}
      <div className="page-header">
        <div className="page-title-row">
          <h1>
            <IconDashboard size={26} color="#818cf8" />
            Dashboard de Autorización IAM Multi-Tenant
          </h1>
          <span className="tenant-badge">
            <span className="pulse-dot" />
            Estado: RLS Activo + Redis Cache
          </span>
        </div>
        <p className="page-desc">
          Plataforma de gobernanza de permisos con arquitectura jerárquica de 3 capas:
          <code>Módulo → Submódulo → Acción</code>.
          Garantiza aislamiento criptográfico por tenant y control de acceso basado en políticas (PBAC/RBAC).
        </p>
      </div>

      {/* 4 Stat Cards */}
      <div className="cards">
        <div className="stat-card">
          <div className="stat-card-icon">
            <IconModules size={22} />
          </div>
          <div className="card-k">{modulos.length}</div>
          <div className="card-v">Módulos Globales</div>
        </div>

        <div className="stat-card emerald">
          <div className="stat-card-icon">
            <IconFolder size={22} />
          </div>
          <div className="card-k">{totalSub}</div>
          <div className="card-v">Submódulos Activos</div>
        </div>

        <div className="stat-card amber">
          <div className="stat-card-icon">
            <IconZap size={22} />
          </div>
          <div className="card-k">{totalAcc}</div>
          <div className="card-v">Acciones Granulares</div>
        </div>

        <div className="stat-card violet">
          <div className="stat-card-icon">
            <IconShield size={22} />
          </div>
          <div className="card-k">RLS V4</div>
          <div className="card-v">JWT Híbrido + Anti-Escalación</div>
        </div>
      </div>

      {/* Architecture & Security Highlights */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '20px', marginBottom: '24px' }}>
        {/* Architecture Card */}
        <div className="panel">
          <div className="panel-header">
            <div className="panel-title">
              <IconSparkles size={16} color="#818cf8" />
              Arquitectura de Permisos Dinámica
            </div>
            <span className="badge emerald">Tiempo Real</span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '10px', padding: '12px 14px' }}>
              <div style={{ fontSize: '13px', fontWeight: 700, color: '#818cf8', marginBottom: '4px' }}>
                1. Módulos & Submódulos (Catálogo Global)
              </div>
              <div className="muted sm">
                Entidades universales sin <code>tenant_id</code>. Cualquier módulo o submódulo agregado en tiempo de ejecución se refleja automáticamente en la barra de navegación lateral.
              </div>
            </div>

            <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '10px', padding: '12px 14px' }}>
              <div style={{ fontSize: '13px', fontWeight: 700, color: '#fbbf24', marginBottom: '4px' }}>
                2. Plantillas & Permisos Directos (Por Tenant)
              </div>
              <div className="muted sm">
                Agrupaciones de roles asignables con cálculo DISTINCT de permisos efectivos. Los cambios en plantillas son retroactivos para todos los usuarios vinculados.
              </div>
            </div>

            <div style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: '10px', padding: '12px 14px' }}>
              <div style={{ fontSize: '13px', fontWeight: 700, color: '#34d399', marginBottom: '4px' }}>
                3. Cache Distribuido & Validación RLS
              </div>
              <div className="muted sm">
                Clave Redis: <code>eff_perms:{'{tenant}'}:{'{user}'}</code>. Evaluación sub-milisegundo para tokens JWT y triggers de base de datos anti-escalación.
              </div>
            </div>
          </div>
        </div>

        {/* Quick Actions & System Info */}
        <div className="panel">
          <div className="panel-header">
            <div className="panel-title">
              <IconServer size={16} color="#38bdf8" />
              Accesos Rápidos & Operaciones
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            <Link to="/usuarios" className="btn" style={{ justifyContent: 'flex-start', padding: '12px 16px' }}>
              <IconUsers size={18} color="#818cf8" />
              <div style={{ textAlign: 'left', marginLeft: '6px' }}>
                <div style={{ fontWeight: 600 }}>Administrar Permisos de Usuarios</div>
                <div className="muted sm">Asignación directa y vinculación de plantillas</div>
              </div>
            </Link>

            <Link to="/modulos" className="btn" style={{ justifyContent: 'flex-start', padding: '12px 16px' }}>
              <IconModules size={18} color="#34d399" />
              <div style={{ textAlign: 'left', marginLeft: '6px' }}>
                <div style={{ fontWeight: 600 }}>Gestionar Catálogo de Módulos</div>
                <div className="muted sm">Crear módulos, submódulos y definir acciones</div>
              </div>
            </Link>

            <Link to="/plantillas" className="btn" style={{ justifyContent: 'flex-start', padding: '12px 16px' }}>
              <IconTemplate size={18} color="#fbbf24" />
              <div style={{ textAlign: 'left', marginLeft: '6px' }}>
                <div style={{ fontWeight: 600 }}>Configurar Plantillas de Rol</div>
                <div className="muted sm">Crear perfiles de acceso reutilizables por tenant</div>
              </div>
            </Link>
          </div>

          <div className="callout info" style={{ marginTop: '16px', fontSize: '12px' }}>
            <div>
              <b>Tip de prueba:</b> Ve a <code>Módulos → + Módulo</code> para añadir un módulo funcional. Verás cómo aparece al instante en el sidebar dinámico sin recargar la página.
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
