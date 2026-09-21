package com.cmtdevsolutions.iam.common.repository;

import com.cmtdevsolutions.iam.common.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByCodigo(String codigo);

    Optional<Tenant> findByNombre(String nombre);

    @Query("SELECT t FROM Tenant t WHERE t.activo = true ORDER BY t.nombre")
    List<Tenant> findAllActivos();

    @Query("SELECT t FROM Tenant t WHERE t.activo = true ORDER BY t.nombre")
    Page<Tenant> findAllActivos(Pageable pageable);

    boolean existsByCodigo(String codigo);

    boolean existsByNombre(String nombre);
}