package com.cmtdevsolutions.iam.common.controller;

import com.cmtdevsolutions.iam.common.dto.UsuarioRequest;
import com.cmtdevsolutions.iam.common.dto.UsuarioResponse;
import com.cmtdevsolutions.iam.common.service.UsuarioService;
import com.cmtdevsolutions.iam.common.filter.TenantContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        Long tenantId = TenantContextHolder.getTenantId();
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(tenantId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        Long tenantId = TenantContextHolder.getTenantId();
        return ResponseEntity.ok(usuarioService.obtenerPorId(tenantId, id));
    }

    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> listar(Pageable pageable) {
        Long tenantId = TenantContextHolder.getTenantId();
        return ResponseEntity.ok(usuarioService.listarPorTenant(tenantId, pageable));
    }

    @GetMapping("/admins")
    public ResponseEntity<List<UsuarioResponse>> listarAdmins() {
        Long tenantId = TenantContextHolder.getTenantId();
        return ResponseEntity.ok(usuarioService.listarAdminsPorTenant(tenantId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        Long tenantId = TenantContextHolder.getTenantId();
        return ResponseEntity.ok(usuarioService.actualizar(tenantId, id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        Long tenantId = TenantContextHolder.getTenantId();
        usuarioService.desactivar(tenantId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        Long tenantId = TenantContextHolder.getTenantId();
        usuarioService.activar(tenantId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> cambiarPassword(@PathVariable Long id, @RequestBody String nuevoPassword) {
        Long tenantId = TenantContextHolder.getTenantId();
        usuarioService.cambiarPassword(tenantId, id, nuevoPassword);
        return ResponseEntity.noContent().build();
    }
}