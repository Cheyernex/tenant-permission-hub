package com.cmtdevsolutions.iam.common.controller;

import com.cmtdevsolutions.iam.common.dto.TenantRequest;
import com.cmtdevsolutions.iam.common.dto.TenantResponse;
import com.cmtdevsolutions.iam.common.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<TenantResponse> crear(@Valid @RequestBody TenantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.crear(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.obtenerPorId(id));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<TenantResponse> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(tenantService.obtenerPorCodigo(codigo));
    }

    @GetMapping
    public ResponseEntity<Page<TenantResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(tenantService.listarActivos(pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TenantResponse> actualizar(@PathVariable Long id, @Valid @RequestBody TenantRequest request) {
        return ResponseEntity.ok(tenantService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        tenantService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        tenantService.activar(id);
        return ResponseEntity.noContent().build();
    }
}