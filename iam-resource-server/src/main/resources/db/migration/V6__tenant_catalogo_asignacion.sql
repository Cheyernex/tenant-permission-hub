-- V6__tenant_catalogo_asignacion.sql
-- Super Admin asigna qué permisos (Submódulo+Acción) están disponibles para cada tenant.
-- Solo lo asignado es visible/usable por el Tenant Admin para crear plantillas y asignaciones.

CREATE TABLE tenant_permiso (
    tenant_id  BIGINT NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    permiso_id BIGINT NOT NULL REFERENCES permiso(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, permiso_id)
);
CREATE INDEX idx_tenant_permiso_tenant ON tenant_permiso(tenant_id);
CREATE INDEX idx_tenant_permiso_permiso ON tenant_permiso(permiso_id);

-- Demo: asignar catálogo inicial a tenant 'demo' (todos los permisos de Productos/Categorias/Ordenes)
-- y a 'system' nada (super admin ve todo sin filtro)
INSERT INTO tenant_permiso (tenant_id, permiso_id)
SELECT t.id, p.id
FROM tenant t, permiso p
JOIN submodulo s ON s.id = p.submodulo_id
WHERE t.codigo = 'demo' AND s.nombre IN ('Productos','Categorias','Ordenes')
ON CONFLICT DO NOTHING;
