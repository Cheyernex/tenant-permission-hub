-- V1__global_schema.sql
-- Estructura global: Módulos, Submódulos, Acciones, Permisos (catálogo)
-- Sin tenant_id - son globales del sistema

CREATE TABLE modulo (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(500),
    orden       INTEGER NOT NULL DEFAULT 0,
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_modulo_activo ON modulo (activo);
CREATE INDEX idx_modulo_nombre ON modulo (nombre);

CREATE TABLE submodulo (
    id          BIGSERIAL PRIMARY KEY,
    modulo_id   BIGINT NOT NULL REFERENCES modulo(id) ON DELETE RESTRICT,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    orden       INTEGER NOT NULL DEFAULT 0,
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX uk_submodulo_modulo_nombre ON submodulo (modulo_id, nombre);
CREATE INDEX idx_submodulo_activo ON submodulo (activo);
CREATE INDEX idx_submodulo_modulo ON submodulo (modulo_id);

CREATE TABLE accion (
    id          BIGSERIAL PRIMARY KEY,
    submodulo_id BIGINT NOT NULL REFERENCES submodulo(id) ON DELETE RESTRICT,
    nombre      VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255),
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX uk_accion_submodulo_nombre ON accion (submodulo_id, nombre);
CREATE INDEX idx_accion_activo ON accion (activo);
CREATE INDEX idx_accion_submodulo ON accion (submodulo_id);

CREATE TABLE permiso (
    id             BIGSERIAL PRIMARY KEY,
    submodulo_id   BIGINT NOT NULL REFERENCES submodulo(id) ON DELETE RESTRICT,
    accion_id      BIGINT NOT NULL REFERENCES accion(id) ON DELETE RESTRICT,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_permiso_submodulo_accion ON permiso (submodulo_id, accion_id);
CREATE INDEX idx_permiso_submodulo ON permiso (submodulo_id);
CREATE INDEX idx_permiso_accion ON permiso (accion_id);

-- Datos iniciales de ejemplo (opcional, para desarrollo)
INSERT INTO modulo (nombre, descripcion, orden) VALUES
    ('Catalogo', 'Gestión de catálogo de productos y categorías', 1),
    ('Pedidos', 'Gestión de pedidos y órdenes de compra', 2),
    ('Pagos', 'Procesamiento y gestión de pagos', 3);

INSERT INTO submodulo (modulo_id, nombre, descripcion, orden)
SELECT id, 'Productos', 'Administración de productos', 1 FROM modulo WHERE nombre = 'Catalogo'
UNION ALL
SELECT id, 'Categorias', 'Administración de categorías', 2 FROM modulo WHERE nombre = 'Catalogo'
UNION ALL
SELECT id, 'Ordenes', 'Gestión de órdenes de compra', 1 FROM modulo WHERE nombre = 'Pedidos'
UNION ALL
SELECT id, 'Historial', 'Historial de pedidos', 2 FROM modulo WHERE nombre = 'Pedidos'
UNION ALL
SELECT id, 'Transacciones', 'Transacciones de pago', 1 FROM modulo WHERE nombre = 'Pagos'
UNION ALL
SELECT id, 'Metodos', 'Métodos de pago', 2 FROM modulo WHERE nombre = 'Pagos';

INSERT INTO accion (submodulo_id, nombre, descripcion)
SELECT s.id, a.nombre, a.descripcion
FROM submodulo s
JOIN (VALUES
    ('Productos', 'CREAR', 'Crear nuevos productos'),
    ('Productos', 'EDITAR', 'Editar productos existentes'),
    ('Productos', 'ELIMINAR', 'Eliminar productos'),
    ('Productos', 'VER', 'Ver detalles de productos'),
    ('Productos', 'EXPORTAR', 'Exportar lista de productos'),
    ('Categorias', 'CREAR', 'Crear nuevas categorías'),
    ('Categorias', 'EDITAR', 'Editar categorías existentes'),
    ('Categorias', 'ELIMINAR', 'Eliminar categorías'),
    ('Categorias', 'VER', 'Ver detalles de categorías'),
    ('Ordenes', 'CREAR', 'Crear nuevas órdenes'),
    ('Ordenes', 'EDITAR', 'Editar órdenes existentes'),
    ('Ordenes', 'VER', 'Ver detalles de órdenes'),
    ('Ordenes', 'CANCELAR', 'Cancelar órdenes'),
    ('Historial', 'VER', 'Ver historial de pedidos'),
    ('Historial', 'EXPORTAR', 'Exportar historial'),
    ('Transacciones', 'CREAR', 'Crear nuevas transacciones'),
    ('Transacciones', 'VER', 'Ver transacciones'),
    ('Transacciones', 'REEMBOLSAR', 'Procesar reembolsos'),
    ('Metodos', 'CREAR', 'Crear métodos de pago'),
    ('Metodos', 'EDITAR', 'Editar métodos de pago'),
    ('Metodos', 'VER', 'Ver métodos de pago'),
    ('Metodos', 'ELIMINAR', 'Eliminar métodos de pago')
) AS a(submodulo_nombre, nombre, descripcion)
ON s.nombre = a.submodulo_nombre;

INSERT INTO permiso (submodulo_id, accion_id)
SELECT a.submodulo_id, a.id
FROM accion a
JOIN submodulo s ON s.id = a.submodulo_id;