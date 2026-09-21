package com.cmtdevsolutions.iam.resource.config;

import com.cmtdevsolutions.iam.common.entity.Tenant;
import com.cmtdevsolutions.iam.common.entity.Usuario;
import com.cmtdevsolutions.iam.common.repository.TenantRepository;
import com.cmtdevsolutions.iam.common.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("docker")
@RequiredArgsConstructor
@Slf4j
public class DemoDataInitializer implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;

    // BCrypt for "password"
    private static final String HASH = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";

    @Override
    @Transactional
    public void run(String... args) {
        try {
            Tenant system = tenantRepository.findByCodigo("system").orElseGet(() -> {
                log.info("Creando tenant demo 'system'");
                return tenantRepository.save(Tenant.builder().nombre("System").codigo("system").descripcion("Tenant sistema SUPER_ADMIN").activo(true).build());
            });
            Tenant demo = tenantRepository.findByCodigo("demo").orElseGet(() -> {
                log.info("Creando tenant demo 'demo'");
                return tenantRepository.save(Tenant.builder().nombre("Demo").codigo("demo").descripcion("Tenant demo").activo(true).build());
            });

            if (usuarioRepository.findByTenantIdAndEmail(system.getId(), "superadmin@system.local").isEmpty()) {
                log.info("Creando superadmin@system.local");
                usuarioRepository.save(Usuario.builder().tenant(system).email("superadmin@system.local").passwordHash(HASH).nombre("Super Admin").esAdmin(true).activo(true).build());
            }
            if (usuarioRepository.findByTenantIdAndEmail(demo.getId(), "admin@demo.local").isEmpty()) {
                log.info("Creando admin@demo.local");
                usuarioRepository.save(Usuario.builder().tenant(demo).email("admin@demo.local").passwordHash(HASH).nombre("Admin Demo").esAdmin(true).activo(true).build());
            }
            if (usuarioRepository.findByTenantIdAndEmail(demo.getId(), "ana@demo.local").isEmpty()) {
                usuarioRepository.save(Usuario.builder().tenant(demo).email("ana@demo.local").passwordHash(HASH).nombre("Ana").esAdmin(false).activo(true).build());
            }
            log.info("Demo data initializer completo — tenants: system({}), demo({})", system.getId(), demo.getId());
        } catch (Exception e) {
            log.warn("DemoDataInitializer fallo (no crítico): {}", e.getMessage());
        }
    }
}
