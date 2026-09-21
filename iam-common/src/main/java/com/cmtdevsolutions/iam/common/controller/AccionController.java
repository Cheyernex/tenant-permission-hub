package com.cmtdevsolutions.iam.common.controller;

import com.cmtdevsolutions.iam.common.dto.AccionRequest;
import com.cmtdevsolutions.iam.common.dto.AccionResponse;
import com.cmtdevsolutions.iam.common.service.AccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/submodulos/{submoduloId}/acciones")
@RequiredArgsConstructor
public class AccionController {

    private final AccionService accionService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AccionResponse> crear(@PathVariable Long submoduloId, @Valid @RequestBody AccionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accionService.crear(submoduloId, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<AccionResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(accionService.obtenerPorId(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<Page<AccionResponse>> listarPorSubmodulo(@PathVariable Long submoduloId, Pageable pageable) {
        return ResponseEntity.ok(accionService.listarPorSubmodulo(submoduloId, pageable));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AccionResponse> actualizar(@PathVariable Long id, @Valid @RequestBody AccionRequest request) {
        return ResponseEntity.ok(accionService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        accionService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        accionService.activar(id);
        return ResponseEntity.noContent().build();
    }
}