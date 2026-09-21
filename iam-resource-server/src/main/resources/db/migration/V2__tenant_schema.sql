-- V2__tenant_schema.sql
-- Entidades por tenant: Tenant, Usuario, Plantilla, Asignaciones
-- Triggers para aislamiento de tenant y anti-escalación (stubs completados en Fases 2-3)

CREATE TABLE tenant (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    codigo      VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(500),
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_tenant_activo ON tenant (activo);
CREATE INDEX idx_tenant_nombre ON tenant (nombre);
CREATE INDEX idx_tenant_codigo ON tenant (codigo);

CREATE TABLE usuario (
    id           BIGSERIAL PRIMARY KEY,
    tenant_id    BIGINT NOT NULL REFERENCES tenant(id) ON DELETE RESTRICT,
    email        VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombre       VARCHAR(150) NOT NULL,
    es_admin     BOOLEAN NOT NULL DEFAULT FALSE,
    activo       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX uk_usuario_tenant_email ON usuario (tenant_id, email);
CREATE INDEX idx_usuario_tenant_activo ON usuario (tenant_id, activo);
CREATE INDEX idx_usuario_admin ON usuario (tenant_id, es_admin);

CREATE TABLE plantilla (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT NOT NULL REFERENCES tenant(id) ON DELETE RESTRICT,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    activa      BOOLEAN NOT NULL DEFAULT TRUE,
    creado_por  BIGINT NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX uk_plantilla_tenant_nombre ON plantilla (tenant_id, nombre);
CREATE INDEX idx_plantilla_tenant_activa ON plantilla (tenant_id, activa);
CREATE INDEX idx_plantilla_creado_por ON plantilla (creado_por);

CREATE TABLE plantilla_permiso (
    plantilla_id BIGINT NOT NULL REFERENCES plantilla(id) ON DELETE CASCADE,
    permiso_id   BIGINT NOT NULL REFERENCES permiso(id) ON DELETE RESTRICT,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (plantilla_id, permiso_id)
);

CREATE INDEX idx_plantilla_permiso_plantilla ON plantilla_permiso (plantilla_id);
CREATE INDEX idx_plantilla_permiso_permiso ON plantilla_permiso (permiso_id);

CREATE TABLE usuario_permiso (
    usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    permiso_id BIGINT NOT NULL REFERENCES permiso(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, permiso_id)
);

CREATE INDEX idx_usuario_permiso_usuario ON usuario_permiso (usuario_id);
CREATE INDEX idx_usuario_permiso_permiso ON usuario_permiso (permiso_id);

CREATE TABLE usuario_plantilla (
    usuario_id   BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    plantilla_id BIGINT NOT NULL REFERENCES plantilla(id) ON DELETE CASCADE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, plantilla_id)
);

CREATE INDEX idx_usuario_plantilla_usuario ON usuario_plantilla (usuario_id);
CREATE INDEX idx_usuario_plantilla_plantilla ON usuario_plantilla (plantilla_id);

-- =====================================================================
-- TRIGGER: Aislamiento de tenant en usuario_plantilla
-- Valida que usuario y plantilla pertenezcan al mismo tenant
-- =====================================================================
CREATE OR REPLACE FUNCTION check_usuario_plantilla_tenant()
RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT tenant_id FROM usuario WHERE id = NEW.usuario_id) <>
       (SELECT tenant_id FROM plantilla WHERE id = NEW.plantilla_id) THEN
        RAISE EXCEPTION 'usuario y plantilla deben pertenecer al mismo tenant (tenant_id mismatch)';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_usuario_plantilla_tenant
BEFORE INSERT OR UPDATE ON usuario_plantilla
FOR EACH ROW EXECUTE FUNCTION check_usuario_plantilla_tenant();

-- =====================================================================
-- TRIGGER STUBS: Anti-escalación de privilegios
-- Lógica completa se implementa en Fases 2-3
-- =====================================================================

-- Stub para validar que el admin (creador/ejecutor) tiene los permisos que intenta asignar
-- En Fase 2: usuario_permiso (asignación directa)
-- En Fase 3: plantilla_permiso (asignación a plantilla)
-- El admin_id se pasa vía SET LOCAL app.current_admin_id = '...' antes del INSERT/UPDATE

CREATE OR REPLACE FUNCTION check_anti_escalacion_usuario_permiso()
RETURNS TRIGGER AS $$
DECLARE
    admin_id BIGINT;
    admin_tiene_permiso BOOLEAN;
BEGIN
    -- Obtener admin_id del contexto de sesión (seteado por la aplicación antes del DML)
    admin_id := NULLIF(current_setting('app.current_admin_id', true), '')::BIGINT;

    IF admin_id IS NULL THEN
        -- En tests o migraciones sin contexto, permitir (la validación real es en service layer)
        RETURN NEW;
    END IF;

    -- Verificar que el admin tiene el permiso efectivamente
    -- (permiso directo O via plantilla asignada)
    SELECT EXISTS (
        SELECT 1
        FROM usuario_permiso up
        WHERE up.usuario_id = admin_id
          AND up.permiso_id = NEW.permiso_id
        UNION
        SELECT 1
        FROM usuario_plantilla upl
        JOIN plantilla p ON p.id = upl.plantilla_id AND p.activa
        JOIN plantilla_permiso pp ON pp.plantilla_id = p.id
        WHERE upl.usuario_id = admin_id
          AND pp.permiso_id = NEW.permiso_id
    ) INTO admin_tiene_permiso;

    IF NOT admin_tiene_permiso THEN
        RAISE EXCEPTION 'anti-escalación: el admin % no posee el permiso %', admin_id, NEW.permiso_id;
    END IF;

    -- Verificar mismo tenant (redundante con FK pero defensivo)
    IF (SELECT tenant_id FROM usuario WHERE id = NEW.usuario_id) <>
       (SELECT tenant_id FROM usuario WHERE id = admin_id) THEN
        RAISE EXCEPTION 'anti-escalación: usuario destino y admin deben ser del mismo tenant';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_usuario_permiso_antiescalada
BEFORE INSERT OR UPDATE ON usuario_permiso
FOR EACH ROW EXECUTE FUNCTION check_anti_escalacion_usuario_permiso();

-- Stub para plantilla_permiso
CREATE OR REPLACE FUNCTION check_anti_escalacion_plantilla_permiso()
RETURNS TRIGGER AS $$
DECLARE
    admin_id BIGINT;
    admin_tiene_permiso BOOLEAN;
    plantilla_tenant BIGINT;
BEGIN
    admin_id := NULLIF(current_setting('app.current_admin_id', true), '')::BIGINT;

    IF admin_id IS NULL THEN
        RETURN NEW;
    END IF;

    -- Tenant de la plantilla
    SELECT tenant_id INTO plantilla_tenant FROM plantilla WHERE id = NEW.plantilla_id;

    -- Verificar que el admin pertenece al mismo tenant
    IF (SELECT tenant_id FROM usuario WHERE id = admin_id) <> plantilla_tenant THEN
        RAISE EXCEPTION 'anti-escalación: admin y plantilla deben ser del mismo tenant';
    END IF;

    -- Verificar que el admin tiene el permiso
    SELECT EXISTS (
        SELECT 1
        FROM usuario_permiso up
        WHERE up.usuario_id = admin_id
          AND up.permiso_id = NEW.permiso_id
        UNION
        SELECT 1
        FROM usuario_plantilla upl
        JOIN plantilla p ON p.id = upl.plantilla_id AND p.activa
        JOIN plantilla_permiso pp ON pp.plantilla_id = p.id
        WHERE upl.usuario_id = admin_id
          AND pp.permiso_id = NEW.permiso_id
    ) INTO admin_tiene_permiso;

    IF NOT admin_tiene_permiso THEN
        RAISE EXCEPTION 'anti-escalación: el admin % no posee el permiso % para incluir en plantilla', admin_id, NEW.permiso_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_plantilla_permiso_antiescalada
BEFORE INSERT OR UPDATE ON plantilla_permiso
FOR EACH ROW EXECUTE FUNCTION check_anti_escalacion_plantilla_permiso();

-- =====================================================================
-- VISTA: Permisos efectivos (unión de directos + via plantillas)
-- Usada en Fase 4 para resolución y cache
-- =====================================================================
CREATE OR REPLACE VIEW v_permisos_efectivos AS
WITH directos AS (
    SELECT u.tenant_id, u.id AS usuario_id, p.submodulo_id, p.accion_id
    FROM usuario_permiso up
    JOIN usuario u ON u.id = up.usuario_id
    JOIN permiso p ON p.id = up.permiso_id
    WHERE u.activo
),
via_plantilla AS (
    SELECT u.tenant_id, u.id AS usuario_id, p.submodulo_id, p.accion_id
    FROM usuario_plantilla upl
    JOIN usuario u ON u.id = upl.usuario_id
    JOIN plantilla pl ON pl.id = upl.plantilla_id AND pl.activa
    JOIN plantilla_permiso pp ON pp.plantilla_id = pl.id
    JOIN permiso p ON p.id = pp.permiso_id
    WHERE u.activo
)
SELECT DISTINCT ON (tenant_id, usuario_id, submodulo_id, accion_id)
    tenant_id, usuario_id, submodulo_id, accion_id
FROM (
    SELECT * FROM directos
    UNION ALL
    SELECT * FROM via_plantilla
) u
ORDER BY tenant_id, usuario_id, submodulo_id, accion_id;