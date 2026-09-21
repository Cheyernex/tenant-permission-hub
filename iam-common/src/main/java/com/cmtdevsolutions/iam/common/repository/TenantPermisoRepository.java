package com.cmtdevsolutions.iam.common.repository;

import com.cmtdevsolutions.iam.common.entity.TenantPermiso;
import com.cmtdevsolutions.iam.common.entity.TenantPermisoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface TenantPermisoRepository extends JpaRepository<TenantPermiso, TenantPermisoId> {
    @Query("SELECT tp.permiso.id FROM TenantPermiso tp WHERE tp.tenant.id = :tenantId")
    Set<Long> findPermisoIdsByTenantId(@Param("tenantId") Long tenantId);

    List<TenantPermiso> findByTenant_Id(Long tenantId);

    boolean existsByTenant_IdAndPermiso_Id(Long tenantId, Long permisoId);

    void deleteByTenant_IdAndPermiso_Id(Long tenantId, Long permisoId);

    void deleteByTenant_Id(Long tenantId);
}
