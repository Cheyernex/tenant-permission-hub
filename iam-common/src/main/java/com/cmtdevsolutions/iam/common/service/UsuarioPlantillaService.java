package com.cmtdevsolutions.iam.common.service;

import com.cmtdevsolutions.iam.common.dto.PlantillaResponse;
import com.cmtdevsolutions.iam.common.dto.PermisoResponse;
import com.cmtdevsolutions.iam.common.entity.Plantilla;
import com.cmtdevsolutions.iam.common.entity.Usuario;
import com.cmtdevsolutions.iam.common.entity.UsuarioPlantilla;
import com.cmtdevsolutions.iam.common.entity.UsuarioPlantillaId;
import com.cmtdevsolutions.iam.common.exception.ResourceNotFoundException;
import com.cmtdevsolutions.iam.common.repository.PlantillaRepository;
import com.cmtdevsolutions.iam.common.repository.UsuarioPlantillaRepository;
import com.cmtdevsolutions.iam.common.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioPlantillaService {

    private final UsuarioRepository usuarioRepository;
    private final PlantillaRepository plantillaRepository;
    private final UsuarioPlantillaRepository usuarioPlantillaRepository;
    private final PermissionValidationService validationService;
    private final PermissionCacheService cacheService;

    @Transactional
    public void asignarPlantilla(Long adminTenantId, Long targetUserId, Long plantillaId) {
        Usuario target = usuarioRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + targetUserId));
        Plantilla plantilla = plantillaRepository.findById(plantillaId)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada: " + plantillaId));

        // Aislamiento tenant: usuario y plantilla mismo tenant que admin
        validationService.validarMismoTenant(adminTenantId, target);
        if (!plantilla.getTenant().getId().equals(adminTenantId)) {
            throw new ResourceNotFoundException("Plantilla no pertenece a tu tenant");
        }
        // Defensa DB también vía trigger trg_usuario_plantilla_tenant

        if (usuarioPlantillaRepository.existsByUsuarioIdAndPlantillaId(targetUserId, plantillaId)) {
            return; // idempotente
        }
        UsuarioPlantilla up = UsuarioPlantilla.builder()
                .id(new UsuarioPlantillaId(targetUserId, plantillaId))
                .usuario(target)
                .plantilla(plantilla)
                .build();
        usuarioPlantillaRepository.save(up);
        cacheService.evict(adminTenantId, targetUserId);
    }

    @Transactional
    public void revocarPlantilla(Long adminTenantId, Long targetUserId, Long plantillaId) {
        Usuario target = usuarioRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + targetUserId));
        validationService.validarMismoTenant(adminTenantId, target);
        usuarioPlantillaRepository.deleteById(new UsuarioPlantillaId(targetUserId, plantillaId));
        cacheService.evict(adminTenantId, targetUserId);
    }

    @Transactional(readOnly = true)
    public List<PlantillaResponse> listarPlantillasDeUsuario(Long adminTenantId, Long targetUserId) {
        Usuario target = usuarioRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + targetUserId));
        validationService.validarMismoTenant(adminTenantId, target);
        return usuarioPlantillaRepository.findByUsuarioIdWithActivePlantillas(targetUserId).stream()
                .map(up -> {
                    Plantilla p = up.getPlantilla();
                    List<PermisoResponse> perms = p.getPermisos() != null ? p.getPermisos().stream()
                            .map(pp -> PermisoResponse.builder()
                                    .id(pp.getPermiso().getId())
                                    .key(pp.getPermiso().getSubmodulo().getNombre() + ":" + pp.getPermiso().getAccion().getNombre())
                                    .submoduloId(pp.getPermiso().getSubmodulo().getId())
                                    .submoduloNombre(pp.getPermiso().getSubmodulo().getNombre())
                                    .accionId(pp.getPermiso().getAccion().getId())
                                    .accionNombre(pp.getPermiso().getAccion().getNombre())
                                    .build()).collect(Collectors.toList()) : List.of();
                    return PlantillaResponse.builder()
                            .id(p.getId())
                            .nombre(p.getNombre())
                            .descripcion(p.getDescripcion())
                            .activa(p.getActiva())
                            .createdAt(p.getCreatedAt())
                            .permisos(perms)
                            .build();
                }).collect(Collectors.toList());
    }
}
