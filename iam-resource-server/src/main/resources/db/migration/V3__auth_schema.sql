-- V3__auth_schema.sql
-- Tablas de Spring Authorization Server (OAuth2/OIDC)
-- Generadas automáticamente por Spring Authorization Server 1.2+
-- Se crean al iniciar la aplicación con spring.jpa.hibernate.ddl-auto=update
-- Esta migración es placeholder para control de versiones

-- Las tablas reales son:
-- oauth2_registered_client
-- oauth2_authorization_consent
-- oauth2_authorization
-- oauth2_device_code
-- oauth2_token

-- Spring Authorization Server las gestiona automáticamente.
-- Si se requiere control manual, exportar DDL con:
-- spring.jpa.properties.javax.persistence.schema-generation.scripts.create-target=create.sql

-- Placeholder para que Flyway tenga versión y no falle
CREATE TABLE IF NOT EXISTS flyway_auth_placeholder (
    id INTEGER PRIMARY KEY DEFAULT 1,
    description VARCHAR(255) NOT NULL DEFAULT 'Spring Authorization Server tables managed by Hibernate',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);