package com.cmtdevsolutions.iam.common.controller;

import com.cmtdevsolutions.iam.common.dto.PermisoAsignacionRequest;
import com.cmtdevsolutions.iam.common.dto.PermisoResponse;
import com.cmtdevsolutions.iam.common.service.TenantCatalogoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/catalogo")
@RequiredArgsConstructor
public class TenantCatalogoController {

    private final TenantCatalogoService tenantCatalogoService;

    // Solo SUPER_ADMIN puede asignar catálogo a tenants
    @PostMapping("/permisos")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<PermisoResponse>> asignar(@PathVariable Long tenantId,
                                                         @Valid @RequestBody PermisoAsignacionRequest req) {
        return ResponseEntity.ok(tenantCatalogoService.asignarPermisos(tenantId, req.getPermisoIds()));
    }

    @GetMapping("/permisos")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','TENANT_ADMIN')")
    public ResponseEntity<List<PermisoResponse>> listar(@PathVariable Long tenantId) {
        return ResponseEntity.ok(tenantCatalogoService.listarAsignados(tenantId));
    }

    @DeleteMapping("/permisos/{permisoId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> revocar(@PathVariable Long tenantId, @PathVariable Long permisoId) {
        tenantCatalogoService.revocarPermiso(tenantId, permisoId);
        return ResponseEntity.noContent().build();
    }

    // Para Tenant Admin: catálogo disponible para su propio tenant (derivado del JWT)
    // Se expone también como /tenants/{id}/catalogo/permisos con check SUPER_ADMIN|TENANT_ADMIN;
    // el frontend para TENANT_ADMIN llamará con su tenantId del JWT.
}
