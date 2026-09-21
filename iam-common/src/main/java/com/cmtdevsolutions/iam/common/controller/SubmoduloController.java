package com.cmtdevsolutions.iam.common.controller;

import com.cmtdevsolutions.iam.common.dto.SubmoduloRequest;
import com.cmtdevsolutions.iam.common.dto.SubmoduloResponse;
import com.cmtdevsolutions.iam.common.service.SubmoduloService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/modulos/{moduloId}/submodulos")
@RequiredArgsConstructor
public class SubmoduloController {

    private final SubmoduloService submoduloService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SubmoduloResponse> crear(@PathVariable Long moduloId, @Valid @RequestBody SubmoduloRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(submoduloService.crear(moduloId, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<SubmoduloResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(submoduloService.obtenerPorId(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<Page<SubmoduloResponse>> listarPorModulo(@PathVariable Long moduloId, Pageable pageable) {
        return ResponseEntity.ok(submoduloService.listarPorModulo(moduloId, pageable));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SubmoduloResponse> actualizar(@PathVariable Long id, @Valid @RequestBody SubmoduloRequest request) {
        return ResponseEntity.ok(submoduloService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        submoduloService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        submoduloService.activar(id);
        return ResponseEntity.noContent().build();
    }
}