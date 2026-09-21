package com.cmtdevsolutions.iam.common.service;

import com.cmtdevsolutions.iam.common.dto.AccionRequest;
import com.cmtdevsolutions.iam.common.dto.AccionResponse;
import com.cmtdevsolutions.iam.common.entity.Accion;
import com.cmtdevsolutions.iam.common.entity.Permiso;
import com.cmtdevsolutions.iam.common.entity.Submodulo;
import com.cmtdevsolutions.iam.common.exception.ResourceNotFoundException;
import com.cmtdevsolutions.iam.common.repository.AccionRepository;
import com.cmtdevsolutions.iam.common.repository.PermisoRepository;
import com.cmtdevsolutions.iam.common.repository.SubmoduloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccionService {

    private final AccionRepository accionRepository;
    private final SubmoduloRepository submoduloRepository;
    private final PermisoRepository permisoRepository;

    @Transactional
    public AccionResponse crear(Long submoduloId, AccionRequest request) {
        Submodulo submodulo = submoduloRepository.findById(submoduloId)
                .orElseThrow(() -> new ResourceNotFoundException("Submódulo padre no encontrado con id: " + submoduloId));

        if (accionRepository.existsBySubmoduloIdAndNombre(submoduloId, request.getNombre())) {
            throw new IllegalArgumentException("Ya existe una acción con el nombre: " + request.getNombre() + " en el submódulo: " + submodulo.getNombre());
        }

        Accion accion = Accion.builder()
                .submodulo(submodulo)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .activo(true)
                .build();

        accion = accionRepository.save(accion);

        // Crear el permiso atómico Submódulo+Acción si no existe (para que aparezca en catálogo y plantillas)
        if (!permisoRepository.existsBySubmoduloIdAndAccionId(submodulo.getId(), accion.getId())) {
            permisoRepository.save(Permiso.builder()
                    .submodulo(submodulo)
                    .accion(accion)
                    .build());
        }

        return toResponse(accion);
    }

    public AccionResponse obtenerPorId(Long id) {
        Accion accion = accionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Acción no encontrada con id: " + id));
        return toResponse(accion);
    }

    public List<AccionResponse> listarPorSubmodulo(Long submoduloId) {
        return accionRepository.findBySubmoduloIdAndActivoTrue(submoduloId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<AccionResponse> listarPorSubmodulo(Long submoduloId, Pageable pageable) {
        return accionRepository.findBySubmoduloIdAndActivoTrue(submoduloId, pageable).map(this::toResponse);
    }

    @Transactional
    public AccionResponse actualizar(Long id, AccionRequest request) {
        Accion accion = accionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Acción no encontrada con id: " + id));

        if (!accion.getNombre().equals(request.getNombre()) &&
            accionRepository.existsBySubmoduloIdAndNombre(accion.getSubmodulo().getId(), request.getNombre())) {
            throw new IllegalArgumentException("Ya existe una acción con el nombre: " + request.getNombre() + " en el submódulo: " + accion.getSubmodulo().getNombre());
        }

        accion.setNombre(request.getNombre());
        accion.setDescripcion(request.getDescripcion());

        accion = accionRepository.save(accion);
        return toResponse(accion);
    }

    @Transactional
    public void desactivar(Long id) {
        Accion accion = accionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Acción no encontrada con id: " + id));
        accion.setActivo(false);
        accionRepository.save(accion);
    }

    @Transactional
    public void activar(Long id) {
        Accion accion = accionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Acción no encontrada con id: " + id));
        accion.setActivo(true);
        accionRepository.save(accion);
    }

    private AccionResponse toResponse(Accion accion) {
        return AccionResponse.builder()
                .id(accion.getId())
                .nombre(accion.getNombre())
                .descripcion(accion.getDescripcion())
                .activo(accion.getActivo())
                .createdAt(accion.getCreatedAt())
                .updatedAt(accion.getUpdatedAt())
                .build();
    }
}