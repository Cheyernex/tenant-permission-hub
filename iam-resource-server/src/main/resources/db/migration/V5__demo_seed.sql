-- V5__demo_seed.sql
-- Datos demo para probar persistencia real sin mocks.
-- Password para todos: "password" (BCrypt $2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW)
-- SUPER_ADMIN no pertenece a tenant normal; se crea tenant 'system' para demo.
-- TENANT_ADMIN demo: tenant 'demo' con admin y usuario normal.

INSERT INTO tenant (nombre, codigo, descripcion, activo) VALUES
  ('System', 'system', 'Tenant sistema para SUPER_ADMIN', true),
  ('Demo', 'demo', 'Tenant demo para pruebas', true)
ON CONFLICT (codigo) DO NOTHING;

-- Super Admin (global) — puede gestionar catálogo
INSERT INTO usuario (tenant_id, email, password_hash, nombre, es_admin, activo)
SELECT t.id, 'superadmin@system.local', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Super Admin', true, true
FROM tenant t WHERE t.codigo = 'system'
ON CONFLICT DO NOTHING;

-- Tenant Demo admin
INSERT INTO usuario (tenant_id, email, password_hash, nombre, es_admin, activo)
SELECT t.id, 'admin@demo.local', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Admin Demo', true, true
FROM tenant t WHERE t.codigo = 'demo'
ON CONFLICT DO NOTHING;

-- Usuario normal demo
INSERT INTO usuario (tenant_id, email, password_hash, nombre, es_admin, activo)
SELECT t.id, 'ana@demo.local', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Ana', false, true
FROM tenant t WHERE t.codigo = 'demo'
ON CONFLICT DO NOTHING;

-- Asignar algunos permisos iniciales al Admin Demo para que pueda delegar sin anti-escalación
-- Admin Demo obtiene todos los permisos de Productos y Categorias para poder crear plantillas
INSERT INTO usuario_permiso (usuario_id, permiso_id)
SELECT u.id, p.id
FROM usuario u
JOIN tenant t ON t.id = u.tenant_id AND t.codigo = 'demo'
JOIN permiso p ON true
JOIN submodulo s ON s.id = p.submodulo_id AND s.nombre IN ('Productos','Categorias')
WHERE u.email = 'admin@demo.local'
ON CONFLICT DO NOTHING;
