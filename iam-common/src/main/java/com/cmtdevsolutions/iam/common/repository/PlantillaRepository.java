package com.cmtdevsolutions.iam.common.repository;

import com.cmtdevsolutions.iam.common.entity.Plantilla;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantillaRepository extends JpaRepository<Plantilla, Long> {

    Optional<Plantilla> findByTenantIdAndNombre(Long tenantId, String nombre);

    @Query("SELECT p FROM Plantilla p WHERE p.tenant.id = :tenantId AND p.activa = true ORDER BY p.nombre")
    List<Plantilla> findByTenantIdAndActivaTrue(Long tenantId);

    @Query("SELECT p FROM Plantilla p WHERE p.tenant.id = :tenantId AND p.activa = true ORDER BY p.nombre")
    Page<Plantilla> findByTenantIdAndActivaTrue(Long tenantId, Pageable pageable);

    @Query("SELECT p FROM Plantilla p LEFT JOIN FETCH p.permisos pp JOIN FETCH pp.permiso perm JOIN FETCH perm.submodulo s JOIN FETCH perm.accion a WHERE p.id = :id")
    Optional<Plantilla> findByIdWithPermisos(Long id);

    @Query("SELECT p FROM Plantilla p WHERE p.tenant.id = :tenantId ORDER BY p.nombre")
    Page<Plantilla> findByTenantId(Long tenantId, Pageable pageable);

    boolean existsByTenantIdAndNombre(Long tenantId, String nombre);
}