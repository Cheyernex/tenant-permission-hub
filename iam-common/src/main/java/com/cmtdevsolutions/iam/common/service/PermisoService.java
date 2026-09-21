package com.cmtdevsolutions.iam.common.service;

import com.cmtdevsolutions.iam.common.dto.PermisoResponse;
import com.cmtdevsolutions.iam.common.entity.Permiso;
import com.cmtdevsolutions.iam.common.exception.ResourceNotFoundException;
import com.cmtdevsolutions.iam.common.repository.PermisoRepository;
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
@Transactional(readOnly = true)
public class PermisoService {

    private final PermisoRepository permisoRepository;

    public PermisoResponse obtenerPorId(Long id) {
        Permiso permiso = permisoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con id: " + id));
        return toResponse(permiso);
    }

    public List<PermisoResponse> listarTodosActivos() {
        return permisoRepository.findAllActivosWithDetails().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<PermisoResponse> listarTodosActivos(Pageable pageable) {
        return permisoRepository.findAllActivosWithDetails(pageable).map(this::toResponse);
    }

    public List<PermisoResponse> listarPorSubmodulo(Long submoduloId) {
        return permisoRepository.findBySubmoduloIdWithDetails(submoduloId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<PermisoResponse> obtenerPorIds(Set<Long> ids) {
        return permisoRepository.findAllByIdWithDetails(ids).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public boolean existe(Long submoduloId, Long accionId) {
        return permisoRepository.existsBySubmoduloIdAndAccionId(submoduloId, accionId);
    }

    private PermisoResponse toResponse(Permiso permiso) {
        return PermisoResponse.builder()
                .id(permiso.getId())
                .key(permiso.getKey())
                .submoduloId(permiso.getSubmodulo().getId())
                .submoduloNombre(permiso.getSubmodulo().getNombre())
                .accionId(permiso.getAccion().getId())
                .accionNombre(permiso.getAccion().getNombre())
                .createdAt(permiso.getCreatedAt())
                .build();
    }
}