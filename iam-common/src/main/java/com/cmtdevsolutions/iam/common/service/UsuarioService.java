package com.cmtdevsolutions.iam.common.service;

import com.cmtdevsolutions.iam.common.dto.UsuarioRequest;
import com.cmtdevsolutions.iam.common.dto.UsuarioResponse;
import com.cmtdevsolutions.iam.common.entity.Tenant;
import com.cmtdevsolutions.iam.common.entity.Usuario;
import com.cmtdevsolutions.iam.common.exception.ResourceNotFoundException;
import com.cmtdevsolutions.iam.common.exception.TenantIsolationException;
import com.cmtdevsolutions.iam.common.repository.TenantRepository;
import com.cmtdevsolutions.iam.common.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Transactional
    public UsuarioResponse crear(Long tenantId, UsuarioRequest request) {
        if (usuarioRepository.existsByTenantIdAndEmail(tenantId, request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + request.getEmail() + " en este tenant");
        }
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado: " + tenantId));

        Usuario usuario = Usuario.builder()
                .tenant(tenant)
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getNombre())
                .esAdmin(request.getEsAdmin())
                .activo(true)
                .build();

        usuario = usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    public UsuarioResponse obtenerPorId(Long tenantId, Long id) {
        Usuario usuario = usuarioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        return toResponse(usuario);
    }

    public List<UsuarioResponse> listarPorTenant(Long tenantId) {
        return usuarioRepository.findByTenantIdAndActivoTrue(tenantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<UsuarioResponse> listarPorTenant(Long tenantId, Pageable pageable) {
        return usuarioRepository.findByTenantIdAndActivoTrue(tenantId, pageable).map(this::toResponse);
    }

    public List<UsuarioResponse> listarAdminsPorTenant(Long tenantId) {
        return usuarioRepository.findAdminsByTenantId(tenantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioResponse actualizar(Long tenantId, Long id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        if (!usuario.getEmail().equals(request.getEmail()) &&
            usuarioRepository.existsByTenantIdAndEmail(tenantId, request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + request.getEmail() + " en este tenant");
        }

        usuario.setEmail(request.getEmail());
        usuario.setNombre(request.getNombre());
        usuario.setEsAdmin(request.getEsAdmin());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        usuario = usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    @Transactional
    public void desactivar(Long tenantId, Long id) {
        Usuario usuario = usuarioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void activar(Long tenantId, Long id) {
        Usuario usuario = usuarioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void cambiarPassword(Long tenantId, Long id, String nuevoPassword) {
        Usuario usuario = usuarioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        usuario.setPasswordHash(passwordEncoder.encode(nuevoPassword));
        usuarioRepository.save(usuario);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .esAdmin(usuario.getEsAdmin())
                .activo(usuario.getActivo())
                .createdAt(usuario.getCreatedAt())
                .updatedAt(usuario.getUpdatedAt())
                .build();
    }
}