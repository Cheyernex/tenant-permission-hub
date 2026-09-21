package com.cmtdevsolutions.iam.common.repository;

import com.cmtdevsolutions.iam.common.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByTenantIdAndEmail(Long tenantId, String email);

    @Query("SELECT u FROM Usuario u WHERE u.tenant.id = :tenantId AND u.activo = true ORDER BY u.nombre")
    List<Usuario> findByTenantIdAndActivoTrue(Long tenantId);

    @Query("SELECT u FROM Usuario u WHERE u.tenant.id = :tenantId AND u.activo = true ORDER BY u.nombre")
    Page<Usuario> findByTenantIdAndActivoTrue(Long tenantId, Pageable pageable);

    @Query("SELECT u FROM Usuario u WHERE u.tenant.id = :tenantId AND u.esAdmin = true AND u.activo = true")
    List<Usuario> findAdminsByTenantId(Long tenantId);

    boolean existsByTenantIdAndEmail(Long tenantId, String email);

    @Query("SELECT u FROM Usuario u WHERE u.id = :id AND u.tenant.id = :tenantId")
    Optional<Usuario> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT u FROM Usuario u WHERE u.tenant.codigo = :codigo AND u.email = :email")
    Optional<Usuario> findByTenant_CodigoAndEmail(String codigo, String email);

    Optional<Usuario> findByEmailIgnoreCase(String email);
}