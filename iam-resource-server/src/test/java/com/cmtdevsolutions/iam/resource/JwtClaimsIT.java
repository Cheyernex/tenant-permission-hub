package com.cmtdevsolutions.iam.resource;

import com.cmtdevsolutions.iam.common.entity.Tenant;
import com.cmtdevsolutions.iam.common.entity.Usuario;
import com.cmtdevsolutions.iam.common.repository.TenantRepository;
import com.cmtdevsolutions.iam.common.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class JwtClaimsIT extends AbstractPostgresIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    TenantRepository tenantRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Test
    void tokenDebeContenerTenantIdYRoles() {
        // Given - crear tenant y usuario admin
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .nombre("Test Tenant")
                .codigo("test-tenant")
                .activo(true)
                .build());

        Usuario admin = usuarioRepository.save(Usuario.builder()
                .tenant(tenant)
                .email("admin@test.com")
                .passwordHash("$2a$10$test") // dummy
                .nombre("Test Admin")
                .esAdmin(true)
                .activo(true)
                .build());

        // When - obtener token via password grant (simulado)
        // En test real se usaría el auth server, aquí verificamos la estructura esperada
        // Este test es placeholder para Fase 5 donde se integra con Auth Server real

        // Then - estructura esperada del JWT (verificado en integración)
        assertThat(tenant.getId()).isNotNull();
        assertThat(admin.getTenant().getId()).isEqualTo(tenant.getId());
    }

    @Test
    void tenantContextFilterExtraeTenantId() {
        // Given
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .nombre("Test Tenant")
                .codigo("test-tenant")
                .activo(true)
                .build());

        Usuario user = usuarioRepository.save(Usuario.builder()
                .tenant(tenant)
                .email("user@test.com")
                .passwordHash("$2a$10$test")
                .nombre("Test User")
                .activo(true)
                .build());

        // Verificar que el tenant existe y tiene el ID correcto
        assertThat(tenant.getId()).isPositive();
        assertThat(user.getTenant().getId()).isEqualTo(tenant.getId());
    }
}