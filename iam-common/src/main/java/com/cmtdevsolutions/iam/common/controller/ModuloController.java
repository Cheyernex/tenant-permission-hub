package com.cmtdevsolutions.iam.common.controller;

import com.cmtdevsolutions.iam.common.dto.ModuloRequest;
import com.cmtdevsolutions.iam.common.dto.ModuloResponse;
import com.cmtdevsolutions.iam.common.service.ModuloService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/modulos")
@RequiredArgsConstructor
public class ModuloController {

    private final ModuloService moduloService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ModuloResponse> crear(@Valid @RequestBody ModuloRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(moduloService.crear(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ModuloResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(moduloService.obtenerPorId(id));
    }

    @GetMapping("/nombre/{nombre}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ModuloResponse> obtenerPorNombre(@PathVariable String nombre) {
        return ResponseEntity.ok(moduloService.obtenerPorNombre(nombre));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<Page<ModuloResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(moduloService.listarActivos(pageable));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ModuloResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ModuloRequest request) {
        return ResponseEntity.ok(moduloService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        moduloService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        moduloService.activar(id);
        return ResponseEntity.noContent().build();
    }
}