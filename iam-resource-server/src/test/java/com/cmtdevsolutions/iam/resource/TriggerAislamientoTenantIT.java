package com.cmtdevsolutions.iam.resource;

import com.cmtdevsolutions.iam.common.entity.Tenant;
import com.cmtdevsolutions.iam.common.entity.Usuario;
import com.cmtdevsolutions.iam.common.entity.Plantilla;
import com.cmtdevsolutions.iam.common.repository.TenantRepository;
import com.cmtdevsolutions.iam.common.repository.UsuarioRepository;
import com.cmtdevsolutions.iam.common.repository.PlantillaRepository;
import com.cmtdevsolutions.iam.common.repository.UsuarioPlantillaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TriggerAislamientoTenantIT extends AbstractPostgresIT {

    @Autowired
    TenantRepository tenantRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PlantillaRepository plantillaRepository;

    @Autowired
    UsuarioPlantillaRepository usuarioPlantillaRepository;

    @Test
    void debePermitirAsignarPlantillaDelMismoTenant() {
        // Given
        Tenant tenantA = tenantRepository.save(Tenant.builder()
                .nombre("Tenant A")
                .codigo("tenant-a")
                .activo(true)
                .build());

        Tenant tenantB = tenantRepository.save(Tenant.builder()
                .nombre("Tenant B")
                .codigo("tenant-b")
                .activo(true)
                .build());

        Usuario adminA = usuarioRepository.save(Usuario.builder()
                .tenant(tenantA)
                .email("admin@tenant-a.com")
                .passwordHash("hash")
                .nombre("Admin A")
                .esAdmin(true)
                .activo(true)
                .build());

        Usuario userA = usuarioRepository.save(Usuario.builder()
                .tenant(tenantA)
                .email("user@tenant-a.com")
                .passwordHash("hash")
                .nombre("User A")
                .activo(true)
                .build());

        Plantilla plantillaA = plantillaRepository.save(Plantilla.builder()
                .tenant(tenantA)
                .nombre("Plantilla A")
                .activa(true)
                .creadoPor(adminA)
                .build());

        // When - asignar plantilla del mismo tenant
        usuarioPlantillaRepository.save(com.cmtdevsolutions.iam.common.entity.UsuarioPlantilla.builder()
                .id(new com.cmtdevsolutions.iam.common.entity.UsuarioPlantillaId(userA.getId(), plantillaA.getId()))
                .usuario(userA)
                .plantilla(plantillaA)
                .build());

        // Then - debe funcionar sin error
        assertThat(usuarioPlantillaRepository.findByUsuarioId(userA.getId())).hasSize(1);
    }

    @Test
    void debeFallarAlAsignarPlantillaDeOtroTenant() {
        // Given
        Tenant tenantA = tenantRepository.save(Tenant.builder()
                .nombre("Tenant A")
                .codigo("tenant-a")
                .activo(true)
                .build());

        Tenant tenantB = tenantRepository.save(Tenant.builder()
                .nombre("Tenant B")
                .codigo("tenant-b")
                .activo(true)
                .build());

        Usuario adminA = usuarioRepository.save(Usuario.builder()
                .tenant(tenantA)
                .email("admin@tenant-a.com")
                .passwordHash("hash")
                .nombre("Admin A")
                .esAdmin(true)
                .activo(true)
                .build());

        Usuario userA = usuarioRepository.save(Usuario.builder()
                .tenant(tenantA)
                .email("user@tenant-a.com")
                .passwordHash("hash")
                .nombre("User A")
                .activo(true)
                .build());

        Plantilla plantillaB = plantillaRepository.save(Plantilla.builder()
                .tenant(tenantB)
                .nombre("Plantilla B")
                .activa(true)
                .creadoPor(adminA) // adminA es de tenantA pero crea plantilla en tenantB (para test)
                .build());

        // When/Then - asignar plantilla de tenantB a usuario de tenantA debe fallar
        assertThatThrownBy(() -> {
            usuarioPlantillaRepository.save(com.cmtdevsolutions.iam.common.entity.UsuarioPlantilla.builder()
                    .id(new com.cmtdevsolutions.iam.common.entity.UsuarioPlantillaId(userA.getId(), plantillaB.getId()))
                    .usuario(userA)
                    .plantilla(plantillaB)
                    .build());
        }).isInstanceOf(Exception.class)
          .hasMessageContaining("tenant");
    }
}