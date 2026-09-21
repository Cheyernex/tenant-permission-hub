package com.cmtdevsolutions.iam.common.service;

import com.cmtdevsolutions.iam.common.dto.AccionResponse;
import com.cmtdevsolutions.iam.common.dto.SubmoduloRequest;
import com.cmtdevsolutions.iam.common.dto.SubmoduloResponse;
import com.cmtdevsolutions.iam.common.entity.Modulo;
import com.cmtdevsolutions.iam.common.entity.Submodulo;
import com.cmtdevsolutions.iam.common.exception.ResourceNotFoundException;
import com.cmtdevsolutions.iam.common.repository.ModuloRepository;
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
public class SubmoduloService {

    private final SubmoduloRepository submoduloRepository;
    private final ModuloRepository moduloRepository;

    @Transactional
    public SubmoduloResponse crear(Long moduloId, SubmoduloRequest request) {
        Modulo modulo = moduloRepository.findById(moduloId)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo padre no encontrado con id: " + moduloId));

        if (submoduloRepository.existsByModuloIdAndNombre(moduloId, request.getNombre())) {
            throw new IllegalArgumentException("Ya existe un submódulo con el nombre: " + request.getNombre() + " en el módulo: " + modulo.getNombre());
        }

        Submodulo submodulo = Submodulo.builder()
                .modulo(modulo)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .orden(request.getOrden())
                .activo(true)
                .build();

        submodulo = submoduloRepository.save(submodulo);
        return toResponseWithAcciones(submodulo);
    }

    public SubmoduloResponse obtenerPorId(Long id) {
        Submodulo submodulo = submoduloRepository.findByIdWithAcciones(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submódulo no encontrado con id: " + id));
        return toResponseWithAcciones(submodulo);
    }

    public List<SubmoduloResponse> listarPorModulo(Long moduloId) {
        return submoduloRepository.findByModuloIdAndActivoTrue(moduloId).stream()
                .map(this::toResponseWithAcciones)
                .collect(Collectors.toList());
    }

    public Page<SubmoduloResponse> listarPorModulo(Long moduloId, Pageable pageable) {
        return submoduloRepository.findByModuloIdAndActivoTrue(moduloId, pageable).map(this::toResponseWithAcciones);
    }

    @Transactional
    public SubmoduloResponse actualizar(Long id, SubmoduloRequest request) {
        Submodulo submodulo = submoduloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submódulo no encontrado con id: " + id));

        if (!submodulo.getNombre().equals(request.getNombre()) &&
            submoduloRepository.existsByModuloIdAndNombre(submodulo.getModulo().getId(), request.getNombre())) {
            throw new IllegalArgumentException("Ya existe un submódulo con el nombre: " + request.getNombre() + " en el módulo: " + submodulo.getModulo().getNombre());
        }

        submodulo.setNombre(request.getNombre());
        submodulo.setDescripcion(request.getDescripcion());
        submodulo.setOrden(request.getOrden());

        submodulo = submoduloRepository.save(submodulo);
        return toResponseWithAcciones(submodulo);
    }

    @Transactional
    public void desactivar(Long id) {
        Submodulo submodulo = submoduloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submódulo no encontrado con id: " + id));
        submodulo.setActivo(false);
        submoduloRepository.save(submodulo);
    }

    @Transactional
    public void activar(Long id) {
        Submodulo submodulo = submoduloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submódulo no encontrado con id: " + id));
        submodulo.setActivo(true);
        submoduloRepository.save(submodulo);
    }

    private SubmoduloResponse toResponseWithAcciones(Submodulo submodulo) {
        SubmoduloResponse response = SubmoduloResponse.builder()
                .id(submodulo.getId())
                .nombre(submodulo.getNombre())
                .descripcion(submodulo.getDescripcion())
                .orden(submodulo.getOrden())
                .activo(submodulo.getActivo())
                .createdAt(submodulo.getCreatedAt())
                .updatedAt(submodulo.getUpdatedAt())
                .build();

        if (submodulo.getAcciones() != null) {
            response.setAcciones(submodulo.getAcciones().stream()
                    .filter(com.cmtdevsolutions.iam.common.entity.Accion::getActivo)
                    .map(this::toAccionResponse)
                    .collect(Collectors.toList()));
        }
        return response;
    }

    private AccionResponse toAccionResponse(com.cmtdevsolutions.iam.common.entity.Accion accion) {
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