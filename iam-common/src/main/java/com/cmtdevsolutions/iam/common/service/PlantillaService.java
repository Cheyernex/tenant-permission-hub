package com.cmtdevsolutions.iam.common.service;

import com.cmtdevsolutions.iam.common.dto.PlantillaRequest;
import com.cmtdevsolutions.iam.common.dto.PlantillaResponse;
import com.cmtdevsolutions.iam.common.dto.PermisoResponse;
import com.cmtdevsolutions.iam.common.entity.*;
import com.cmtdevsolutions.iam.common.exception.ResourceNotFoundException;
import com.cmtdevsolutions.iam.common.repository.PermisoRepository;
import com.cmtdevsolutions.iam.common.repository.PlantillaPermisoRepository;
import com.cmtdevsolutions.iam.common.repository.PlantillaRepository;
import com.cmtdevsolutions.iam.common.repository.UsuarioPlantillaRepository;
import com.cmtdevsolutions.iam.common.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlantillaService {

    private final PlantillaRepository plantillaRepository;
    private final PermisoRepository permisoRepository;
    private final PlantillaPermisoRepository plantillaPermisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioPlantillaRepository usuarioPlantillaRepository;
    private final PermissionValidationService validationService;
    private final PermissionCacheService cacheService;

    @PersistenceContext
    private EntityManager entityManager;

    private void setCurrentAdminId(Long adminId) {
        entityManager.createNativeQuery("SELECT set_config('app.current_admin_id', :id, true)")
                .setParameter("id", String.valueOf(adminId))
                .getSingleResult();
    }

    @Transactional
    public PlantillaResponse crear(Long adminUserId, Long adminTenantId, PlantillaRequest request) {
        Usuario admin = usuarioRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin no encontrado: " + adminUserId));
        // Nombre único por tenant
        if (plantillaRepository.existsByTenantIdAndNombre(adminTenantId, request.getNombre())) {
            throw new IllegalArgumentException("Ya existe una plantilla con nombre '" + request.getNombre() + "' en este tenant");
        }
        // Anti-escalación + catálogo asignado por Super Admin + existencia en catálogo global
        if (request.getPermisoIds() != null && !request.getPermisoIds().isEmpty()) {
            validarCatalogoTenant(adminTenantId, request.getPermisoIds());
            validationService.validarAntiEscalacion(adminUserId, request.getPermisoIds());
            List<Permiso> permisos = permisoRepository.findAllById(request.getPermisoIds());
            if (permisos.size() != request.getPermisoIds().size()) {
                Set<Long> encontrados = permisos.stream().map(Permiso::getId).collect(Collectors.toSet());
                Set<Long> faltantes = request.getPermisoIds().stream().filter(id -> !encontrados.contains(id)).collect(Collectors.toSet());
                throw new ResourceNotFoundException("Permisos no encontrados en catálogo: " + faltantes);
            }
        }

        Tenant tenantRef = entityManager.getReference(Tenant.class, adminTenantId);
        Plantilla plantilla = Plantilla.builder()
                .tenant(tenantRef)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .activa(true)
                .creadoPor(admin)
                .build();
        plantilla = plantillaRepository.save(plantilla);

        if (request.getPermisoIds() != null && !request.getPermisoIds().isEmpty()) {
            setCurrentAdminId(adminUserId);
            for (Long pid : request.getPermisoIds()) {
                Permiso perm = entityManager.getReference(Permiso.class, pid);
                PlantillaPermiso pp = PlantillaPermiso.builder()
                        .id(new PlantillaPermisoId(plantilla.getId(), pid))
                        .plantilla(plantilla)
                        .permiso(perm)
                        .build();
                plantillaPermisoRepository.save(pp);
            }
        }
        return toResponse(plantilla.getId());
    }

    @Transactional(readOnly = true)
    public PlantillaResponse obtener(Long tenantId, Long plantillaId) {
        Plantilla p = plantillaRepository.findByIdWithPermisos(plantillaId)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada: " + plantillaId));
        if (!p.getTenant().getId().equals(tenantId)) {
            throw new ResourceNotFoundException("Plantilla no pertenece a tu tenant");
        }
        return toResponseDto(p);
    }

    @Transactional(readOnly = true)
    public Page<PlantillaResponse> listar(Long tenantId, Pageable pageable) {
        return plantillaRepository.findByTenantId(tenantId, pageable).map(pl -> toResponse(pl.getId()));
    }

    @Transactional
    public PlantillaResponse actualizar(Long adminUserId, Long tenantId, Long plantillaId, PlantillaRequest request) {
        Plantilla p = plantillaRepository.findById(plantillaId)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada: " + plantillaId));
        if (!p.getTenant().getId().equals(tenantId)) {
            throw new ResourceNotFoundException("Plantilla no pertenece a tu tenant");
        }
        if (!p.getNombre().equals(request.getNombre()) && plantillaRepository.existsByTenantIdAndNombre(tenantId, request.getNombre())) {
            throw new IllegalArgumentException("Ya existe una plantilla con nombre '" + request.getNombre() + "'");
        }
        // Si se cambian permisos, validar catálogo + anti-escalación
        if (request.getPermisoIds() != null && !request.getPermisoIds().isEmpty()) {
            validarCatalogoTenant(tenantId, request.getPermisoIds());
            validationService.validarAntiEscalacion(adminUserId, request.getPermisoIds());
        }
        p.setNombre(request.getNombre());
        p.setDescripcion(request.getDescripcion());
        plantillaRepository.save(p);

        if (request.getPermisoIds() != null) {
            // reemplazar permisos: borrar existentes y re-insertar (dentro misma transacción, trigger validará)
            setCurrentAdminId(adminUserId);
            plantillaPermisoRepository.deleteByPlantillaId(plantillaId);
            // flush para que delete se ejecute antes de inserts y no viole PK
            entityManager.flush();
            for (Long pid : request.getPermisoIds()) {
                Permiso perm = entityManager.getReference(Permiso.class, pid);
                PlantillaPermiso pp = PlantillaPermiso.builder()
                        .id(new PlantillaPermisoId(plantillaId, pid))
                        .plantilla(p)
                        .permiso(perm)
                        .build();
                plantillaPermisoRepository.save(pp);
            }
            evictUsuariosDePlantilla(tenantId, plantillaId);
        }
        return toResponse(plantillaId);
    }

    @Transactional
    public void desactivar(Long tenantId, Long plantillaId) {
        Plantilla p = plantillaRepository.findById(plantillaId)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada: " + plantillaId));
        if (!p.getTenant().getId().equals(tenantId)) {
            throw new ResourceNotFoundException("Plantilla no pertenece a tu tenant");
        }
        p.setActiva(false);
        plantillaRepository.save(p);
        evictUsuariosDePlantilla(tenantId, plantillaId);
    }

    @Transactional
    public List<PermisoResponse> agregarPermisos(Long adminUserId, Long tenantId, Long plantillaId, Set<Long> permisoIds) {
        Plantilla p = plantillaRepository.findById(plantillaId)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada: " + plantillaId));
        if (!p.getTenant().getId().equals(tenantId)) {
            throw new ResourceNotFoundException("Plantilla no pertenece a tu tenant");
        }
        validarCatalogoTenant(tenantId, permisoIds);
        validationService.validarAntiEscalacion(adminUserId, permisoIds);
        setCurrentAdminId(adminUserId);
        for (Long pid : permisoIds) {
            if (!plantillaPermisoRepository.existsByPlantillaIdAndPermisoId(plantillaId, pid)) {
                Permiso perm = entityManager.getReference(Permiso.class, pid);
                plantillaPermisoRepository.save(PlantillaPermiso.builder()
                        .id(new PlantillaPermisoId(plantillaId, pid))
                        .plantilla(p)
                        .permiso(perm)
                        .build());
            }
        }
        evictUsuariosDePlantilla(tenantId, plantillaId);
        return toResponse(plantillaId).getPermisos();
    }

    @Transactional
    public void removerPermiso(Long tenantId, Long plantillaId, Long permisoId) {
        Plantilla p = plantillaRepository.findById(plantillaId)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada: " + plantillaId));
        if (!p.getTenant().getId().equals(tenantId)) {
            throw new ResourceNotFoundException("Plantilla no pertenece a tu tenant");
        }
        plantillaPermisoRepository.deleteById(new PlantillaPermisoId(plantillaId, permisoId));
        evictUsuariosDePlantilla(tenantId, plantillaId);
    }

    private PlantillaResponse toResponse(Long plantillaId) {
        Plantilla p = plantillaRepository.findByIdWithPermisos(plantillaId)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada: " + plantillaId));
        return toResponseDto(p);
    }

    private void validarCatalogoTenant(Long tenantId, Set<Long> permisoIds) {
        if (permisoIds == null || permisoIds.isEmpty()) return;
        Tenant t = entityManager.find(Tenant.class, tenantId);
        if (t != null && "system".equalsIgnoreCase(t.getCodigo())) return;
        @SuppressWarnings("unchecked")
        List<Number> rows = entityManager.createNativeQuery("SELECT permiso_id FROM tenant_permiso WHERE tenant_id = :tid")
                .setParameter("tid", tenantId).getResultList();
        Set<Long> asignados = rows.stream().map(Number::longValue).collect(Collectors.toSet());
        if (asignados.isEmpty()) {
            throw new IllegalArgumentException("Tenant sin catálogo asignado por Super Admin. Solicita al Super Admin que asigne permisos a tu tenant en /tenants/" + tenantId + "/catalogo");
        }
        Set<Long> noAsignados = permisoIds.stream().filter(id -> !asignados.contains(id)).collect(Collectors.toSet());
        if (!noAsignados.isEmpty()) {
            throw new IllegalArgumentException("Permisos no asignados a tu tenant por Super Admin: " + noAsignados + ". Disponibles: " + asignados);
        }
    }

    private void evictUsuariosDePlantilla(Long tenantId, Long plantillaId) {
        try {
            @SuppressWarnings("unchecked")
            List<Number> rows = entityManager.createNativeQuery(
                    "SELECT usuario_id FROM usuario_plantilla WHERE plantilla_id = :pid")
                    .setParameter("pid", plantillaId).getResultList();
            Set<Long> userIds = rows.stream().map(Number::longValue).collect(Collectors.toSet());
            cacheService.evictByUserIds(tenantId, userIds);
        } catch (Exception ignored) {}
    }

    private PlantillaResponse toResponseDto(Plantilla p) {
        List<PermisoResponse> perms = p.getPermisos() != null ? p.getPermisos().stream()
                .map(pp -> PermisoResponse.builder()
                        .id(pp.getPermiso().getId())
                        .key(pp.getPermiso().getSubmodulo().getNombre() + ":" + pp.getPermiso().getAccion().getNombre())
                        .submoduloId(pp.getPermiso().getSubmodulo().getId())
                        .submoduloNombre(pp.getPermiso().getSubmodulo().getNombre())
                        .accionId(pp.getPermiso().getAccion().getId())
                        .accionNombre(pp.getPermiso().getAccion().getNombre())
                        .createdAt(pp.getCreatedAt())
                        .build()).collect(Collectors.toList()) : List.of();
        return PlantillaResponse.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .activa(p.getActiva())
                .creadoPorId(p.getCreadoPor() != null ? p.getCreadoPor().getId() : null)
                .creadoPorNombre(p.getCreadoPor() != null ? p.getCreadoPor().getNombre() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .permisos(perms)
                .build();
    }
}
