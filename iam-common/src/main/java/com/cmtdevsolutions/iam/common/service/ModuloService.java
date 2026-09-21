package com.cmtdevsolutions.iam.common.service;

import com.cmtdevsolutions.iam.common.dto.ModuloRequest;
import com.cmtdevsolutions.iam.common.dto.ModuloResponse;
import com.cmtdevsolutions.iam.common.dto.SubmoduloResponse;
import com.cmtdevsolutions.iam.common.entity.Modulo;
import com.cmtdevsolutions.iam.common.entity.Submodulo;
import com.cmtdevsolutions.iam.common.exception.ResourceNotFoundException;
import com.cmtdevsolutions.iam.common.repository.ModuloRepository;
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
public class ModuloService {

    private final ModuloRepository moduloRepository;

    @Transactional
    public ModuloResponse crear(ModuloRequest request) {
        if (moduloRepository.existsByNombre(request.getNombre())) {
            throw new IllegalArgumentException("Ya existe un módulo con el nombre: " + request.getNombre());
        }

        Modulo modulo = Modulo.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .orden(request.getOrden())
                .activo(true)
                .build();

        modulo = moduloRepository.save(modulo);
        return toResponse(modulo);
    }

    public ModuloResponse obtenerPorId(Long id) {
        Modulo modulo = moduloRepository.findByIdWithSubmodulos(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado con id: " + id));
        return toResponseWithSubmodulos(modulo);
    }

    public ModuloResponse obtenerPorNombre(String nombre) {
        Modulo modulo = moduloRepository.findByNombre(nombre)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado con nombre: " + nombre));
        return toResponseWithSubmodulos(modulo);
    }

    public List<ModuloResponse> listarActivos() {
        return moduloRepository.findAllActivos().stream()
                .map(this::toResponseWithSubmodulos)
                .collect(Collectors.toList());
    }

    public Page<ModuloResponse> listarActivos(Pageable pageable) {
        return moduloRepository.findAllActivos(pageable).map(this::toResponseWithSubmodulos);
    }

    @Transactional
    public ModuloResponse actualizar(Long id, ModuloRequest request) {
        Modulo modulo = moduloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado con id: " + id));

        if (!modulo.getNombre().equals(request.getNombre()) && moduloRepository.existsByNombre(request.getNombre())) {
            throw new IllegalArgumentException("Ya existe un módulo con el nombre: " + request.getNombre());
        }

        modulo.setNombre(request.getNombre());
        modulo.setDescripcion(request.getDescripcion());
        modulo.setOrden(request.getOrden());

        modulo = moduloRepository.save(modulo);
        return toResponseWithSubmodulos(modulo);
    }

    @Transactional
    public void desactivar(Long id) {
        Modulo modulo = moduloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado con id: " + id));
        modulo.setActivo(false);
        moduloRepository.save(modulo);
    }

    @Transactional
    public void activar(Long id) {
        Modulo modulo = moduloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado con id: " + id));
        modulo.setActivo(true);
        moduloRepository.save(modulo);
    }

    private ModuloResponse toResponse(Modulo modulo) {
        return ModuloResponse.builder()
                .id(modulo.getId())
                .nombre(modulo.getNombre())
                .descripcion(modulo.getDescripcion())
                .orden(modulo.getOrden())
                .activo(modulo.getActivo())
                .createdAt(modulo.getCreatedAt())
                .updatedAt(modulo.getUpdatedAt())
                .build();
    }

    private ModuloResponse toResponseWithSubmodulos(Modulo modulo) {
        ModuloResponse response = toResponse(modulo);
        if (modulo.getSubmodulos() != null) {
            response.setSubmodulos(modulo.getSubmodulos().stream()
                    .filter(Submodulo::getActivo)
                    .map(this::toSubmoduloResponse)
                    .collect(Collectors.toList()));
        }
        return response;
    }

    private SubmoduloResponse toSubmoduloResponse(com.cmtdevsolutions.iam.common.entity.Submodulo submodulo) {
        return SubmoduloResponse.builder()
                .id(submodulo.getId())
                .nombre(submodulo.getNombre())
                .descripcion(submodulo.getDescripcion())
                .orden(submodulo.getOrden())
                .activo(submodulo.getActivo())
                .createdAt(submodulo.getCreatedAt())
                .updatedAt(submodulo.getUpdatedAt())
                .build();
    }
}