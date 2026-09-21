package com.cmtdevsolutions.iam.common.service;

import com.cmtdevsolutions.iam.common.dto.PermisoResponse;
import com.cmtdevsolutions.iam.common.entity.Permiso;
import com.cmtdevsolutions.iam.common.entity.Usuario;
import com.cmtdevsolutions.iam.common.entity.UsuarioPermiso;
import com.cmtdevsolutions.iam.common.entity.UsuarioPermisoId;
import com.cmtdevsolutions.iam.common.exception.ResourceNotFoundException;
import com.cmtdevsolutions.iam.common.repository.PermisoRepository;
import com.cmtdevsolutions.iam.common.repository.UsuarioPermisoRepository;
import com.cmtdevsolutions.iam.common.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioPermisoService {

    private final UsuarioRepository usuarioRepository;
    private final PermisoRepository permisoRepository;
    private final UsuarioPermisoRepository usuarioPermisoRepository;
    private final PermissionValidationService validationService;
    private final PermissionResolutionService resolutionService;
    private final PermissionCacheService cacheService;

    @PersistenceContext
    private EntityManager entityManager;

    private void setCurrentAdminId(Long adminId) {
        entityManager.createNativeQuery("SELECT set_config('app.current_admin_id', :id, true)")
                .setParameter("id", String.valueOf(adminId))
                .getSingleResult();
    }

    @Transactional
    public List<PermisoResponse> asignarPermisos(Long adminUserId, Long adminTenantId, Long targetUserId, Set<Long> permisoIds) {
        Usuario target = usuarioRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario destino no encontrado: " + targetUserId));
        validationService.validarMismoTenant(adminTenantId, target);
        validationService.validarAntiEscalacion(adminUserId, permisoIds);

        // validar que los permisoIds existen en catálogo global
        List<Permiso> permisos = permisoRepository.findAllById(permisoIds);
        if (permisos.size() != permisoIds.size()) {
            Set<Long> encontrados = permisos.stream().map(Permiso::getId).collect(Collectors.toSet());
            Set<Long> faltantes = permisoIds.stream().filter(id -> !encontrados.contains(id)).collect(Collectors.toSet());
            throw new ResourceNotFoundException("Permisos no encontrados en catálogo: " + faltantes);
        }

        setCurrentAdminId(adminUserId);

        for (Permiso p : permisos) {
            if (!usuarioPermisoRepository.existsByUsuarioIdAndPermisoId(targetUserId, p.getId())) {
                UsuarioPermiso up = UsuarioPermiso.builder()
                        .id(new UsuarioPermisoId(targetUserId, p.getId()))
                        .usuario(target)
                        .permiso(p)
                        .build();
                usuarioPermisoRepository.save(up);
            }
        }
        cacheService.evict(adminTenantId, targetUserId);
        // Si el admin se asigna a sí mismo, su cache también debe invalidarse para anti-escalación futura consistente
        if (!adminUserId.equals(targetUserId)) {
            // no evict admin salvo que sea target
        }
        return listarPermisosDirectos(targetUserId);
    }

    @Transactional
    public void revocarPermiso(Long adminTenantId, Long targetUserId, Long permisoId) {
        Usuario target = usuarioRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario destino no encontrado: " + targetUserId));
        validationService.validarMismoTenant(adminTenantId, target);

        UsuarioPermisoId id = new UsuarioPermisoId(targetUserId, permisoId);
        if (!usuarioPermisoRepository.existsById(id)) {
            throw new ResourceNotFoundException("El usuario no tiene asignado el permiso: " + permisoId);
        }
        usuarioPermisoRepository.deleteById(id);
        cacheService.evict(adminTenantId, targetUserId);
    }

    @Transactional(readOnly = true)
    public List<PermisoResponse> listarPermisosDirectos(Long targetUserId) {
        return usuarioPermisoRepository.findByUsuarioIdWithDetails(targetUserId).stream()
                .map(up -> PermisoResponse.builder()
                        .id(up.getPermiso().getId())
                        .key(up.getPermiso().getSubmodulo().getNombre() + ":" + up.getPermiso().getAccion().getNombre())
                        .submoduloId(up.getPermiso().getSubmodulo().getId())
                        .submoduloNombre(up.getPermiso().getSubmodulo().getNombre())
                        .accionId(up.getPermiso().getAccion().getId())
                        .accionNombre(up.getPermiso().getAccion().getNombre())
                        .createdAt(up.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PermisoResponse> listarPermisosEfectivos(Long targetUserId, Long adminTenantId) {
        Usuario target = usuarioRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + targetUserId));
        validationService.validarMismoTenant(adminTenantId, target);
        return cacheService.getEffectivePermisosCached(adminTenantId, targetUserId);
    }
}
