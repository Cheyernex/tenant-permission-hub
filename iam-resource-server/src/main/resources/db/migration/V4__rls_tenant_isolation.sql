-- V4__rls_tenant_isolation.sql
-- Fase 5 Hardening: Row Level Security para aislamiento tenant (defense-in-depth).
-- Fases 1-4 usan validación en service layer + triggers; esta migración activa RLS
-- y debe ejecutarse con un rol con BYPASSRLS (ej. postgres/owner).
-- La app setea app.current_tenant_id por request vía SET LOCAL (TenantContextFilter).

-- Habilitar RLS en tablas por-tenant
ALTER TABLE usuario ENABLE ROW LEVEL SECURITY;
ALTER TABLE plantilla ENABLE ROW LEVEL SECURITY;
ALTER TABLE usuario_permiso ENABLE ROW LEVEL SECURITY;
ALTER TABLE usuario_plantilla ENABLE ROW LEVEL SECURITY;
ALTER TABLE plantilla_permiso ENABLE ROW LEVEL SECURITY;

-- Políticas: solo filas del tenant del request pueden leerse/escribirse.
-- Si app.current_tenant_id no está seteado (jobs, SUPER_ADMIN con tenant 0), permitir todo para superuser.
-- La app SIEMPRE setea app.current_tenant_id desde JWT; SUPER_ADMIN usa issuer sin RLS (bypass) o tenant_id = 0.

DROP POLICY IF EXISTS tenant_isolation_usuario ON usuario;
CREATE POLICY tenant_isolation_usuario ON usuario
    USING (tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
           OR current_setting('app.current_tenant_id', true) = '' 
           OR current_setting('app.current_tenant_id', true) IS NULL)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                OR current_setting('app.current_tenant_id', true) = ''
                OR current_setting('app.current_tenant_id', true) IS NULL);

DROP POLICY IF EXISTS tenant_isolation_plantilla ON plantilla;
CREATE POLICY tenant_isolation_plantilla ON plantilla
    USING (tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
           OR current_setting('app.current_tenant_id', true) = ''
           OR current_setting('app.current_tenant_id', true) IS NULL)
    WITH CHECK (tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                OR current_setting('app.current_tenant_id', true) = ''
                OR current_setting('app.current_tenant_id', true) IS NULL);

-- Tablas de asignación: validar vía join al owner (usuario o plantilla)
DROP POLICY IF EXISTS tenant_isolation_usuario_permiso ON usuario_permiso;
CREATE POLICY tenant_isolation_usuario_permiso ON usuario_permiso
    USING (EXISTS (SELECT 1 FROM usuario u WHERE u.id = usuario_id
                   AND (u.tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                        OR current_setting('app.current_tenant_id', true) = ''
                        OR current_setting('app.current_tenant_id', true) IS NULL)))
    WITH CHECK (EXISTS (SELECT 1 FROM usuario u WHERE u.id = usuario_id
                        AND (u.tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                             OR current_setting('app.current_tenant_id', true) = ''
                             OR current_setting('app.current_tenant_id', true) IS NULL)));

DROP POLICY IF EXISTS tenant_isolation_usuario_plantilla ON usuario_plantilla;
CREATE POLICY tenant_isolation_usuario_plantilla ON usuario_plantilla
    USING (EXISTS (SELECT 1 FROM usuario u WHERE u.id = usuario_id
                   AND (u.tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                        OR current_setting('app.current_tenant_id', true) = ''
                        OR current_setting('app.current_tenant_id', true) IS NULL)))
    WITH CHECK (EXISTS (SELECT 1 FROM usuario u WHERE u.id = usuario_id
                        AND (u.tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                             OR current_setting('app.current_tenant_id', true) = ''
                             OR current_setting('app.current_tenant_id', true) IS NULL)));

DROP POLICY IF EXISTS tenant_isolation_plantilla_permiso ON plantilla_permiso;
CREATE POLICY tenant_isolation_plantilla_permiso ON plantilla_permiso
    USING (EXISTS (SELECT 1 FROM plantilla p WHERE p.id = plantilla_id
                   AND (p.tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                        OR current_setting('app.current_tenant_id', true) = ''
                        OR current_setting('app.current_tenant_id', true) IS NULL)))
    WITH CHECK (EXISTS (SELECT 1 FROM plantilla p WHERE p.id = plantilla_id
                        AND (p.tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                             OR current_setting('app.current_tenant_id', true) = ''
                             OR current_setting('app.current_tenant_id', true) IS NULL)));

-- Nota: Tablas globales modulo/submodulo/accion/permiso NO llevan RLS (visibles para todos los tenants).
-- Force RLS incluso para owner (opcional, descomentar si se quiere forzar en todos los roles):
-- ALTER TABLE usuario FORCE ROW LEVEL SECURITY;
-- ALTER TABLE plantilla FORCE ROW LEVEL SECURITY;
