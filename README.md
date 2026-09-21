# Tenant Permission Hub — IAM Modular con Delegación por Tenant/Scope

Sistema de permisos modulares con delegación administrativa, aislamiento por tenant y validación anti-escalación en **service + trigger PostgreSQL + RLS opcional**. Stack: Java 21, Spring Boot 3.3, Spring Security + Authorization Server (JWT), PostgreSQL 16, Redis 7, React.

> **Diagrama interactivo:** `docs/tenant-permission-hub.html` — generado con [archify](https://github.com/tt-a1i/archify) (`showcase` 9/9 checks). Abrir directo en el navegador (SVG inline, Dark/Light, export PNG/SVG).

---

## 1) Arquitectura

```
Browser (Admin/SPA :5173) ──OAuth2/token──▶ Auth Server :9000 ──┐
        │ Bearer JWT (tenant_id, user_id, roles, permissions, perm_version) │
        └──────────────────────▶ Resource Server :8080 ◀──────────────────────┘
                                    │  │  │
                    JPA/Flyway V1-V4│  │  └─Redis eff_perms:{tenant}:{user} TTL 10m
                                    │  └─TenantContextFilter (JWT → ThreadLocal)
                                    └─iam-common (10 entidades, 10 repos, services)
                                          │
                              PostgreSQL 16 (triggers + RLS V4)
```

**Servicios Docker (`docker-compose.yml:1`):**

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| `postgres:16-alpine` | 5432 | `iam_db` / `iam_user` / `iam_pass` — Flyway V1-V4, triggers, RLS |
| `redis:7-alpine` | 6379 | Cache `eff_perms:{tenant}:{user}` |
| `iam-auth-server` | 9000 | Spring Authorization Server — emite JWT híbrido |
| `iam-resource-server` | 8080 | REST API + validación tenant/anti-escalación |
| `iam-web` | 5173 | Vite + React 18 — sidebar dinámico |

**Módulos Maven:**

```
iam-parent/
├── iam-common/            # Entidades, Repos, Services, DTOs, Security, RedisConfig
├── iam-auth-server/       # :9000 — AuthorizationServerConfig + OAuth2TokenCustomizerConfig
├── iam-resource-server/   # :8080 — ResourceServerSecurityConfig + TenantContextFilter + Flyway V1-V4
└── iam-web/               # :5173 — React UI dockerizada (serve)
```

---

## 2) Requisitos

- Java 21, Maven 3.9+, Node 20+ (solo para `iam-web` local), Docker & Docker Compose

---

## 3) Quick Start

```bash
# 1) Infra + apps (requiere internet para eclipse-temurin/node pull)
docker-compose up -d --build
# Swagger: http://localhost:8080/swagger-ui.html
# Web:     http://localhost:5173
# Auth JWKS: http://localhost:9000/oauth2/jwks

# Sin internet (usa imágenes cacheadas + run local):
docker-compose up -d postgres redis
mvn -pl iam-auth-server -am spring-boot:run -Dspring-boot.run.profiles=docker &
mvn -pl iam-resource-server -am spring-boot:run -Dspring-boot.run.profiles=docker &
npm --prefix iam-web run dev  # http://localhost:5173
```

**Validar:**
```bash
docker-compose ps # 4-5 healthy
curl http://localhost:8080/actuator/health
curl http://localhost:9000/.well-known/openid-configuration
```

---

## 4) Endpoints REST (`/api/v1` — `springdoc` en `:8080`)

**Auth Server `:9000`**

| Método | Path | Auth |
|--------|------|------|
| `POST` | `/oauth2/token` (`grant_type=client_credentials` / `authorization_code` + `client_id=iam-client`) | `Basic iam-client:secret` |
| `GET` | `/oauth2/jwks`, `/.well-known/openid-configuration` | — |

**Resource Server `:8080` — tenant siempre del JWT (`SecurityUtils.java:7`), nunca del body/query**

| Grupo | Método | Path | Rol | Validación |
|-------|--------|------|-----|------------|
| **Tenants** | `POST` | `/tenants` | `SUPER_ADMIN` | — |
| | `GET` | `/tenants`, `/tenants/{id}`, `/tenants/codigo/{codigo}` | `SUPER_ADMIN` | — |
| | `PATCH` | `/tenants/{id}`, `/tenants/{id}/desactivar|activar` | `SUPER_ADMIN` | soft delete |
| **Módulos** | `POST` | `/modulos` | `SUPER_ADMIN` | `UK(nombre)` |
| | `GET` | `/modulos`, `/modulos/{id}`, `/modulos/nombre/{n}` | `SUPER_ADMIN`/`TENANT_ADMIN` | read-only tenant |
| | `PATCH` | `/modulos/{id}`, `/{id}/desactivar|activar` | `SUPER_ADMIN` | soft delete |
| **Submódulos** | `POST` | `/modulos/{moduloId}/submodulos` | `SUPER_ADMIN` | `UK(modulo,nombre)` |
| | `GET` | `/modulos/{moduloId}/submodulos`, `/submodulos/{id}` | `SUPER_ADMIN`/`TENANT_ADMIN` | — |
| | `PATCH` | `/submodulos/{id}`, `/{id}/desactivar|activar` | `SUPER_ADMIN` | — |
| **Acciones** | `POST` | `/submodulos/{subId}/acciones` | `SUPER_ADMIN` | `UK(submodulo,nombre)` — no es enum global |
| | `GET` | `/submodulos/{subId}/acciones` | `SUPER_ADMIN`/`TENANT_ADMIN` | — |
| | `PATCH` | `/acciones/{id}`, `/{id}/desactivar|activar` | `SUPER_ADMIN` | — |
| **Usuarios** | `POST` | `/usuarios` | `TENANT_ADMIN` | `UK(tenant,email)` |
| | `GET` | `/usuarios`, `/usuarios/{id}`, `/usuarios/admins` | `TENANT_ADMIN` | `WHERE tenant_id = :jwt` |
| | `PATCH` | `/usuarios/{id}`, `/{id}/desactivar|activar|password` | `TENANT_ADMIN` | — |
| **Permisos catálogo** | `GET` | (vía `PermisoService.java:17`) | `TENANT_ADMIN` | `JOIN submodulo/accion WHERE activo` |
| **Usuario-Permiso (Fase 2)** | `POST` | `/usuarios/{usuarioId}/permisos` `{permisoIds}` | `TENANT_ADMIN` | **8a+8b**: `PermissionValidationService:13` + `SET LOCAL app.current_admin_id` → `trg_usuario_permiso_antiescalada` |
| | `DELETE` | `/usuarios/{usuarioId}/permisos/{permisoId}` | `TENANT_ADMIN` | `tenant check` + `evict cache` |
| | `GET` | `/usuarios/{usuarioId}/permisos` (directos), `/permisos/efectivos` | `TENANT_ADMIN` | `UNION` + `DISTINCT` + cache |
| **Plantillas (Fase 3)** | `POST` | `/plantillas` `{nombre, descripcion, permisoIds}` | `TENANT_ADMIN` | `UK(tenant,nombre)` + **8a** + trigger `trg_plantilla_permiso_antiescalada` |
| | `GET` | `/plantillas`, `/plantillas/{id}` | `TENANT_ADMIN` | `tenant_id = :jwt` |
| | `PATCH` | `/plantillas/{id}` | `TENANT_ADMIN` | re-valida **8a**, `DELETE+flush+INSERT` + `evictUsuariosDePlantilla` |
| | `DELETE` | `/plantillas/{id}` | `TENANT_ADMIN` | soft `activa=false` + evict |
| | `POST` | `/plantillas/{id}/permisos` `{permisoIds}` | `TENANT_ADMIN` | **8a** |
| | `DELETE` | `/plantillas/{id}/permisos/{permisoId}` | `TENANT_ADMIN` | + evict |
| **Usuario-Plantilla** | `POST` | `/usuarios/{usuarioId}/plantillas/{plantillaId}` | `TENANT_ADMIN` | `trg_usuario_plantilla_tenant` + evict |
| | `DELETE` | `/usuarios/{usuarioId}/plantillas/{plantillaId}` | `TENANT_ADMIN` | evict |
| | `GET` | `/usuarios/{usuarioId}/plantillas` | `TENANT_ADMIN` | — |
| **Efectivos (Fase 4)** | `GET` | `/mi-perfil/permisos-efectivos` | `isAuthenticated()` | `PermissionCacheService:14` `eff_perms:{tenant}:{user}` TTL 10m |

---

## 5) Autenticación — JWT Híbrido (Fase 5)

**Obtener token:**

```bash
# 1) Authorization Code (recomendado) — abre http://localhost:9000/oauth2/authorize?response_type=code&client_id=iam-client&scope=openid%20profile%20read%20write

# 2) Client Credentials (demo)
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "iam-client:secret" \
  -d "grant_type=client_credentials&scope=read write"
```

**Usar en Resource Server:**

```bash
curl -H "Authorization: Bearer <access_token>" http://localhost:8080/api/v1/usuarios
```

**Claims (`OAuth2TokenCustomizerConfig.java:14` — `JwtEncodingContext`):**

```json
{
  "sub": "user@demo.local",
  "tenant_id": 1,
  "user_id": 12,
  "email": "user@demo.local",
  "nombre": "User",
  "roles": ["TENANT_ADMIN"],
  "permissions": ["Productos:VER", "Productos:EDITAR"],
  "perm_version": -123456
}
```

`permissions` resuelto vía `PermissionCacheService.java:14` (`UNION directos+plantillas` + `Redis`). Access token 30m (`AuthorizationServerConfig.java:59`); **validación 8a/8b siempre contra DB/cache en service layer**, no solo JWT — revocación inmediata.

---

## 6) Modelo de Datos (ERD textual)

**Globales (sin `tenant_id` — catálogo reutilizable):**

- `modulo(id, nombre UK, descripcion, orden, activo)` — e.g. Catálogo, Pedidos, Pagos
- `submodulo(id, modulo_id FK, nombre UK(modulo,nombre), activo)` — e.g. Productos, Categorías (hijo 1 módulo)
- `accion(id, submodulo_id FK, nombre UK(submodulo,nombre), activo)` — e.g. CREAR, VER (varía por submódulo)
- `permiso(id, submodulo_id FK, accion_id FK, UK(submodulo,accion))` — atómico Submódulo+Acción

**Por tenant (con `tenant_id`):**

- `tenant(id, nombre UK, codigo UK, activo)`
- `usuario(id, tenant_id FK NOT NULL, email UK(tenant,email), password_hash, es_admin, activo)`
- `plantilla(id, tenant_id FK NOT NULL, nombre UK(tenant,nombre), activa, creado_por FK usuario)`
- `plantilla_permiso(plantilla_id FK, permiso_id FK) PK(compuesta)` — `plantilla.tenant_id` implícito
- `usuario_permiso(usuario_id FK, permiso_id FK) PK` — `usuario.tenant_id` implícito
- `usuario_plantilla(usuario_id FK, plantilla_id FK) PK` — **CHECK vía trigger** `usuario.tenant_id == plantilla.tenant_id`

**Constraints que refuerzan aislamiento (Fase 5):**

- `V2__tenant_schema.sql:24` `TRIGGER trg_usuario_plantilla_tenant BEFORE INSERT OR UPDATE` (`RAISE EXCEPTION 'tenant_id mismatch'`)
- `trg_usuario_permiso_antiescalada` + `trg_plantilla_permiso_antiescalada` — `current_setting('app.current_admin_id')` + `EXISTS (UP directo UNION UP vía plantilla)`
- `V4__rls_tenant_isolation.sql:1` `ENABLE ROW LEVEL SECURITY` + policies `USING/WITH CHECK (tenant_id = current_setting('app.current_tenant_id')::bigint)` — Fases 1-4 usan service+triggers, RLS se activa en hardening y `TenantContextFilter.java:22` hace `SET LOCAL app.current_tenant_id` por request.

---

## 7) Modelo de Autorización

- **Efectivos:** `PERMISOS_EFECTIVOS(u) = DIRECTOS(u) ∪ ⋃ PERMISOS_DE_PLANTILLA(p)` (`PermissionResolutionService.java:18` query única `SELECT ... UNION` + `DISTINCT`, sin N+1).
- **Cache:** `PermissionCacheService.java:14` `eff_perms:{tenant}:{user}` JSON `List<PermisoResponse>` TTL 10m (`StringRedisTemplate` + `ObjectMapper`); `Optional` para run sin Redis (dev/test). Invalidación en `UsuarioPermisoService:55`, `UsuarioPlantillaService:46`, `PlantillaService:28` (`evict` + `evictByUserIds` vía `SELECT usuario_id FROM usuario_plantilla WHERE plantilla_id=:pid`).
- **8a Anti-escalación:** `PermissionValidationService:13` `validarAntiEscalacion(adminId, permisoIds)` = `permisoIds ⊆ getEffectivePermisoIds(adminId)` else `PrivilegeEscalationException` (403). Reforzado por trigger DB.
- **8b Aislamiento:** `validarMismoTenant(adminTenantId, target)` + trigger/RLS; `TenantContextHolder.java:7` ThreadLocal + `TenantContextFilter.java:22` extrae `tenant_id` del JWT.
- **Tenant del request:** nunca del body/query — `SecurityUtils.java:7` `getCurrentTenantId()/getCurrentUserId()` desde `JwtAuthenticationToken`.

---

## 8) Stack Técnico

| Capa | Tech |
|------|------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.3.2, Spring Security 6, Spring Authorization Server 1.3.1, Spring Data JPA, Flyway 10.18.0 |
| JWT | `spring-security-oauth2-jose`, `JwtGrantedAuthoritiesConverter` → `PERM_*` / `ROLE_*` / `TENANT_*` |
| Cache | Redis 7 (Lettuce) `spring-boot-starter-data-redis` |
| DB | PostgreSQL 16, HikariCP |
| Docs | `springdoc-openapi-starter-webmvc-ui 2.6.0` |
| Tests | JUnit5 + Testcontainers `postgresql:16` (sin H2 — triggers PL/pgSQL reales) |
| Frontend | Vite 5 + React 18 + TS + React Router 6 — `iam-web/` |

---

## 9) UI Web — Sidebar Dinámico (`iam-web/`)

`Vite` proxy `/api → :8080`, `/oauth2 → :9000`. `Sidebar.tsx:7` genera módulos/submódulos dinámicamente desde `GET /api/v1/modulos` (fallback `seedModulos.ts:7` idéntico a `V1`).

- **Dashboard** — contadores + callout sidebar dinámico
- **Módulos** — CRUD Módulos (global, SUPER_ADMIN), Submódulos/Acciones, soft-delete `activo` toggle. Crear aquí aparece al instante en sidebar.
- **Usuarios** — por tenant, asignación directa con preview `UNION` efectivos
- **Plantillas** — crear por tenant (`UK(tenant,nombre)`) con `permisoIds` validados, edición retroactiva
- **Login** — mock JWT `tenantCodigo:email` + `role` (prod: `POST /oauth2/token`)

```bash
npm --prefix iam-web install && npm --prefix iam-web run dev # :5173
docker-compose up -d --build web # :5173 serve
```

CORS habilitado `CorsConfig.java:7` para `http://localhost:5173`.

---

## 10) Reglas de Negocio No Negociables

1. **8a Anti-escalación:** `PlantillaService:43` / `UsuarioPermisoService:42` + triggers DB.
2. **8b Aislamiento:** `UsuarioPlantillaService:29` + `V4` RLS. `SUPER_ADMIN` gestiona `modulo/submodulo/accion` (`@PreAuthorize("hasRole('SUPER_ADMIN')")`), `TENANT_ADMIN` solo lectura global.

---

## 11) Casos Borde Resueltos

| Caso | Decisión |
|------|----------|
| Eliminar Submódulo/Acción en uso | **Soft delete** (`activo=false`) — preserva FK/history, UI filtra `activo=true` |
| Permiso vía plantilla + directo | Permitido (UNION idempotente). Revocar plantilla no borra directo |
| Editar plantilla asignada | **Retroactivo** (reemplazo `plantilla_permiso` + `evictUsuariosDePlantilla`) — si se necesita inmutabilidad, crear `v2` |
| Admin pierde permiso ya delegado | **No cascada** — permanece en usuarios; solo bloquea nuevas delegaciones |
| Gestión catálogo global | Solo `SUPER_ADMIN` (`ModuloController.java:13`) |
| Super Admin elimina Acción/Submódulo global en uso | Soft delete + warning; job reporta `activo=false` con referencias |

---

## 12) Roadmap por Fases (completadas)

| Fase | Entregable | Estado |
|------|------------|--------|
| 1 | Entidades globales+tenant, Flyway V1-V3, Auth Server JWT `tenant_id/roles`, RLS off | ✅ |
| 2 | Permisos directos + `PermissionValidationService` + triggers + `SET LOCAL app.current_admin_id` | ✅ |
| 3 | Plantillas + `UsuarioPlantillaService` + triggers | ✅ |
| 4 | Vista `v_permisos_efectivos` (CTE UNION DISTINCT) + Redis `eff_perms` + `MiPerfilController` | ✅ |
| 5 | `OAuth2TokenCustomizerConfig` JWT híbrido + Redis en auth-server + `V4` RLS + `TenantContextFilter` hardening | ✅ |
| UI | `iam-web` sidebar dinámico + CRUD | ✅ |

---

## 13) Tests & Calidad

```bash
mvn test # Testcontainers PG real (no H2)
mvn test -Dtest=TriggerAislamientoTenantIT,JwtClaimsIT
```

- `AbstractPostgresIT.java:7` base Testcontainers `postgres:16`
- `TriggerAislamientoTenantIT.java:7` — mismo tenant OK, cross-tenant `RAISE EXCEPTION 'tenant_id mismatch'`
- `JwtClaimsIT.java:7` — `tenant_id/user_id/roles` en JWT
- `mvn clean package -DskipTests` — `BUILD SUCCESS` (verificado Fase 5)

---

## 14) Configuración

Perfiles `application.yml:1` (`dev`/`docker`/`test`):

- `dev`: `ddl-auto: validate`, `flyway: true`, `show-sql: true`
- `docker`: `ddl-auto: update` (auth-server) / `validate` (resource-server), `spring.data.redis.host=redis`, `flyway enabled false` (resource-server `@Profile("!docker")` por PG 16.15), `issuer-uri: http://auth-server:9000`
- `test`: `ContainerDatabaseDriver` (`jdbc:tc:postgresql:16:///iam_test`)

---

## 15) Estado Git

- `master` → `github.com/Cheyernex/tenant-permission-hub`
- Últimos: `e8eded0` fix TenantContextFilter, `34ff41b` UI, `56b0517` Fases 2-5, `f42d550` Fase 1
