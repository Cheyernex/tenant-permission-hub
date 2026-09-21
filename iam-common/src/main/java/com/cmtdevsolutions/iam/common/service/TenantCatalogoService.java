package com.cmtdevsolutions.iam.common.service;

import com.cmtdevsolutions.iam.common.dto.PermisoResponse;
import com.cmtdevsolutions.iam.common.entity.Permiso;
import com.cmtdevsolutions.iam.common.entity.Tenant;
import com.cmtdevsolutions.iam.common.entity.TenantPermiso;
import com.cmtdevsolutions.iam.common.entity.TenantPermisoId;
import com.cmtdevsolutions.iam.common.exception.ResourceNotFoundException;
import com.cmtdevsolutions.iam.common.repository.PermisoRepository;
import com.cmtdevsolutions.iam.common.repository.TenantPermisoRepository;
import com.cmtdevsolutions.iam.common.repository.TenantRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantCatalogoService {

    private final TenantRepository tenantRepository;
    private final PermisoRepository permisoRepository;
    private final TenantPermisoRepository tenantPermisoRepository;
    private final EntityManager entityManager;

    @Transactional
    public List<PermisoResponse> asignarPermisos(Long tenantId, Set<Long> permisoIds) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado: " + tenantId));
        List<Permiso> perms = permisoRepository.findAllById(permisoIds);
        if (perms.size() != permisoIds.size()) {
            Set<Long> found = perms.stream().map(Permiso::getId).collect(Collectors.toSet());
            Set<Long> miss = permisoIds.stream().filter(id -> !found.contains(id)).collect(Collectors.toSet());
            throw new ResourceNotFoundException("Permisos no encontrados: " + miss);
        }
        for (Permiso p : perms) {
            if (!tenantPermisoRepository.existsByTenant_IdAndPermiso_Id(tenantId, p.getId())) {
                tenantPermisoRepository.save(TenantPermiso.builder()
                        .id(new TenantPermisoId(tenantId, p.getId()))
                        .tenant(tenant)
                        .permiso(p)
                        .build());
            }
        }
        return listarAsignados(tenantId);
    }

    @Transactional(readOnly = true)
    public List<PermisoResponse> listarAsignados(Long tenantId) {
        tenantRepository.findById(tenantId).orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado: " + tenantId));
        return tenantPermisoRepository.findByTenant_Id(tenantId).stream()
                .map(tp -> {
                    Permiso p = tp.getPermiso();
                    return PermisoResponse.builder()
                            .id(p.getId())
                            .key(p.getSubmodulo().getNombre() + ":" + p.getAccion().getNombre())
                            .submoduloId(p.getSubmodulo().getId())
                            .submoduloNombre(p.getSubmodulo().getNombre())
                            .accionId(p.getAccion().getId())
                            .accionNombre(p.getAccion().getNombre())
                            .createdAt(tp.getCreatedAt())
                            .build();
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PermisoResponse> listarDisponibles(Long tenantId) {
        // Para SUPER_ADMIN: si tenant no tiene asignaciones, devuelve vacío (debe asignar primero).
        // Para TENANT_ADMIN: este es el catálogo que puede usar.
        return listarAsignados(tenantId);
    }

    @Transactional
    public void revocarPermiso(Long tenantId, Long permisoId) {
        tenantPermisoRepository.deleteByTenant_IdAndPermiso_Id(tenantId, permisoId);
    }

    @Transactional
    public void revocarTodos(Long tenantId) {
        tenantPermisoRepository.deleteByTenant_Id(tenantId);
    }

    @Transactional(readOnly = true)
    public Set<Long> getPermisoIdsAsignados(Long tenantId) {
        return tenantPermisoRepository.findPermisoIdsByTenantId(tenantId);
    }
}
