import type { Modulo } from '../types/iam'

export function Dashboard({ modulos }: { modulos: Modulo[] }) {
  const totalSub = modulos.reduce((a, m) => a + m.submodulos.length, 0)
  const totalAcc = modulos.reduce((a, m) => a + m.submodulos.reduce((s, sub) => s + sub.acciones.length, 0), 0)
  return (
    <div className="page">
      <h1>Dashboard</h1>
      <p className="muted">Arquitectura: Módulo → Submódulo → Acción. Permiso = Submódulo + Acción. Plantillas por tenant. Cache Redis <code>eff_perms:{'{tenant}'}:{'{user}'}</code> + RLS V4.</p>
      <div className="cards">
        <div className="card"><div className="card-k">3</div><div className="card-v">Módulos globales</div></div>
        <div className="card"><div className="card-k">{totalSub}</div><div className="card-v">Submódulos</div></div>
        <div className="card"><div className="card-k">{totalAcc}</div><div className="card-v">Acciones</div></div>
        <div className="card"><div className="card-k">Fase 5</div><div className="card-v">JWT híbrido + RLS</div></div>
      </div>
      <div className="callout">
        <b>Cómo probar el sidebar dinámico:</b> Ve a <code> Módulos → Nuevo módulo</code> o añade un submódulo/acción. Aparece al instante en el sidebar sin recargar (los módulos son globales, reutilizables por todos los tenants).
      </div>
    </div>
  )
}
