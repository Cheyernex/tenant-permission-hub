package com.cmtdevsolutions.iam.common.service;

import com.cmtdevsolutions.iam.common.dto.TenantRequest;
import com.cmtdevsolutions.iam.common.dto.TenantResponse;
import com.cmtdevsolutions.iam.common.entity.Tenant;
import com.cmtdevsolutions.iam.common.exception.ResourceNotFoundException;
import com.cmtdevsolutions.iam.common.repository.TenantRepository;
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
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional
    public TenantResponse crear(TenantRequest request) {
        if (tenantRepository.existsByCodigo(request.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un tenant con el código: " + request.getCodigo());
        }
        if (tenantRepository.existsByNombre(request.getNombre())) {
            throw new IllegalArgumentException("Ya existe un tenant con el nombre: " + request.getNombre());
        }

        Tenant tenant = Tenant.builder()
                .nombre(request.getNombre())
                .codigo(request.getCodigo())
                .descripcion(request.getDescripcion())
                .activo(true)
                .build();

        tenant = tenantRepository.save(tenant);
        return toResponse(tenant);
    }

    public TenantResponse obtenerPorId(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado con id: " + id));
        return toResponse(tenant);
    }

    public TenantResponse obtenerPorCodigo(String codigo) {
        Tenant tenant = tenantRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado con código: " + codigo));
        return toResponse(tenant);
    }

    public List<TenantResponse> listarActivos() {
        return tenantRepository.findAllActivos().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<TenantResponse> listarActivos(Pageable pageable) {
        return tenantRepository.findAllActivos(pageable).map(this::toResponse);
    }

    @Transactional
    public TenantResponse actualizar(Long id, TenantRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado con id: " + id));

        if (!tenant.getCodigo().equals(request.getCodigo()) && tenantRepository.existsByCodigo(request.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un tenant con el código: " + request.getCodigo());
        }
        if (!tenant.getNombre().equals(request.getNombre()) && tenantRepository.existsByNombre(request.getNombre())) {
            throw new IllegalArgumentException("Ya existe un tenant con el nombre: " + request.getNombre());
        }

        tenant.setNombre(request.getNombre());
        tenant.setCodigo(request.getCodigo());
        tenant.setDescripcion(request.getDescripcion());

        tenant = tenantRepository.save(tenant);
        return toResponse(tenant);
    }

    @Transactional
    public void desactivar(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado con id: " + id));
        tenant.setActivo(false);
        tenantRepository.save(tenant);
    }

    @Transactional
    public void activar(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado con id: " + id));
        tenant.setActivo(true);
        tenantRepository.save(tenant);
    }

    private TenantResponse toResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .nombre(tenant.getNombre())
                .codigo(tenant.getCodigo())
                .descripcion(tenant.getDescripcion())
                .activo(tenant.getActivo())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}