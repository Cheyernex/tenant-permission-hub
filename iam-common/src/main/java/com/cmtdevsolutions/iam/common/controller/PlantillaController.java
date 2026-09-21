package com.cmtdevsolutions.iam.common.controller;

import com.cmtdevsolutions.iam.common.dto.PlantillaRequest;
import com.cmtdevsolutions.iam.common.dto.PlantillaResponse;
import com.cmtdevsolutions.iam.common.dto.PermisoAsignacionRequest;
import com.cmtdevsolutions.iam.common.dto.PermisoResponse;
import com.cmtdevsolutions.iam.common.security.SecurityUtils;
import com.cmtdevsolutions.iam.common.service.PlantillaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plantillas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class PlantillaController {

    private final PlantillaService plantillaService;

    @PostMapping
    public ResponseEntity<PlantillaResponse> crear(@Valid @RequestBody PlantillaRequest request) {
        Long adminId = SecurityUtils.getCurrentUserId();
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.status(HttpStatus.CREATED).body(plantillaService.crear(adminId, tenantId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantillaResponse> obtener(@PathVariable Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(plantillaService.obtener(tenantId, id));
    }

    @GetMapping
    public ResponseEntity<Page<PlantillaResponse>> listar(Pageable pageable) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(plantillaService.listar(tenantId, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PlantillaResponse> actualizar(@PathVariable Long id, @Valid @RequestBody PlantillaRequest request) {
        Long adminId = SecurityUtils.getCurrentUserId();
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(plantillaService.actualizar(adminId, tenantId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        plantillaService.desactivar(tenantId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/permisos")
    public ResponseEntity<List<PermisoResponse>> agregarPermisos(@PathVariable Long id,
                                                                 @Valid @RequestBody PermisoAsignacionRequest request) {
        Long adminId = SecurityUtils.getCurrentUserId();
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(plantillaService.agregarPermisos(adminId, tenantId, id, request.getPermisoIds()));
    }

    @DeleteMapping("/{id}/permisos/{permisoId}")
    public ResponseEntity<Void> removerPermiso(@PathVariable Long id, @PathVariable Long permisoId) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        plantillaService.removerPermiso(tenantId, id, permisoId);
        return ResponseEntity.noContent().build();
    }
}
