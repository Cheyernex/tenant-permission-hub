# IAM - Identity and Access Management

Sistema de permisos modulares con delegación administrativa por tenant.

## Arquitectura Multi-módulo

```
iam-parent/
├── iam-common/           # Entidades, Repositorios, Servicios, Controladores compartidos
├── iam-auth-server/      # Spring Authorization Server (puerto 9000)
└── iam-resource-server/  # Resource Server con lógica de negocio (puerto 8080)
```

## Requisitos

- Java 21
- Maven 3.9+
- Docker & Docker Compose (para PostgreSQL y Redis)

## Levantar infraestructura

```bash
docker-compose up -d
```

Esto levanta:
- PostgreSQL 16 en `localhost:5432` (db: `iam_db`, user: `iam_user`, pass: `iam_pass`)
- Redis 7 en `localhost:6379`

## Compilar y ejecutar

### Auth Server (puerto 9000)
```bash
cd iam-auth-server
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Resource Server (puerto 8080)
```bash
cd iam-resource-server
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Endpoints principales

### Auth Server (9000)
- `POST /oauth2/token` - Obtener access token
- `GET /oauth2/jwks` - Claves públicas JWKS
- `GET /.well-known/openid-configuration` - OIDC Discovery

### Resource Server (8080)

#### Tenants (Solo SUPER_ADMIN)
- `POST /api/v1/tenants` - Crear tenant
- `GET /api/v1/tenants` - Listar tenants
- `GET /api/v1/tenants/{id}` - Obtener tenant
- `PATCH /api/v1/tenants/{id}` - Actualizar tenant
- `PATCH /api/v1/tenants/{id}/desactivar` - Soft delete

#### Módulos (SUPER_ADMIN write, TENANT_ADMIN read)
- `POST /api/v1/modulos` - Crear módulo
- `GET /api/v1/modulos` - Listar módulos
- `GET /api/v1/modulos/{id}` - Obtener módulo con submódulos
- `PATCH /api/v1/modulos/{id}` - Actualizar
- `PATCH /api/v1/modulos/{id}/desactivar` - Soft delete

#### Submódulos
- `POST /api/v1/modulos/{moduloId}/submodulos` - Crear
- `GET /api/v1/modulos/{moduloId}/submodulos` - Listar
- `GET /api/v1/submodulos/{id}` - Obtener con acciones
- `PATCH /api/v1/submodulos/{id}` - Actualizar
- `PATCH /api/v1/submodulos/{id}/desactivar` - Soft delete

#### Acciones
- `POST /api/v1/submodulos/{submoduloId}/acciones` - Crear
- `GET /api/v1/submodulos/{submoduloId}/acciones` - Listar
- `GET /api/v1/acciones/{id}` - Obtener
- `PATCH /api/v1/acciones/{id}` - Actualizar
- `PATCH /api/v1/acciones/{id}/desactivar` - Soft delete

#### Usuarios (TENANT_ADMIN - scoped a su tenant)
- `POST /api/v1/usuarios` - Crear usuario
- `GET /api/v1/usuarios` - Listar usuarios del tenant
- `GET /api/v1/usuarios/{id}` - Obtener usuario
- `PATCH /api/v1/usuarios/{id}` - Actualizar
- `PATCH /api/v1/usuarios/{id}/desactivar` - Soft delete
- `PATCH /api/v1/usuarios/{id}/activar` - Reactivar
- `PATCH /api/v1/usuarios/{id}/password` - Cambiar password

## Autenticación

1. Obtener token del Auth Server:
```bash
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "iam-client:secret" \
  -d "grant_type=password&username=tenantCodigo:email&password=password&scope=openid profile read write"
```

2. Usar token en Resource Server:
```bash
curl -H "Authorization: Bearer <access_token>" http://localhost:8080/api/v1/usuarios
```

### Claims del JWT
El access token incluye:
```json
{
  "sub": "user_id",
  "tenant_id": 1,
  "user_id": 123,
  "email": "user@tenant.com",
  "nombre": "Usuario",
  "roles": ["TENANT_ADMIN"],
  "permissions": ["productos:CREAR", "productos:VER"],
  "perm_version": 1
}
```

## Base de datos

Migraciones Flyway en `iam-resource-server/src/main/resources/db/migration/`:
- `V1__global_schema.sql` - Módulos, Submódulos, Acciones, Permisos (catálogo global)
- `V2__tenant_schema.sql` - Tenants, Usuarios, Plantillas, Asignaciones + Triggers
- `V3__auth_schema.sql` - Tablas OAuth2 (gestionadas por Hibernate)

### Triggers críticos (V2)
- `trg_usuario_plantilla_tenant` - Valida mismo tenant en asignación usuario-plantilla
- `trg_usuario_permiso_antiescalada` - Anti-escalación en asignación directa
- `trg_plantilla_permiso_antiescalada` - Anti-escalación en plantillas

## Tests

```bash
# Solo tests de resource server (usan Testcontainers)
cd iam-resource-server
mvn test

# Tests específicos
mvn test -Dtest=TriggerAislamientoTenantIT
```

## Documentación API

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Roles

- `SUPER_ADMIN` - Gestiona estructura global (módulos, submódulos, acciones) y tenants
- `TENANT_ADMIN` - Gestiona usuarios, plantillas y asignaciones dentro de su tenant
- `USER` - Usuario final (consume permisos via JWT)

## Reglas de negocio (Fase 1 base)

1. **Aislamiento de tenant**: Un admin nunca ve/modifica datos de otro tenant (validado en service + trigger BD)
2. **Anti-escalación**: Un admin solo puede delegar permisos que él mismo posee (service + trigger BD)
3. **Soft delete**: Módulos, submódulos, acciones, tenants, usuarios, plantillas usan `activo=false`
4. **Estructura global**: Módulos/Submódulos/Acciones no tienen tenant_id, son catálogo compartido

## Próximas fases

- **Fase 2**: Permisos directos a usuario con validación completa
- **Fase 3**: Plantillas con anti-escalación completa
- **Fase 4**: Resolución de permisos efectivos + Cache Redis
- **Fase 5**: Integración completa Auth Server + JWT con permisos reales