package com.cmtdevsolutions.iam.common.controller;

import com.cmtdevsolutions.iam.common.dto.PermisoAsignacionRequest;
import com.cmtdevsolutions.iam.common.dto.PermisoResponse;
import com.cmtdevsolutions.iam.common.security.SecurityUtils;
import com.cmtdevsolutions.iam.common.service.UsuarioPermisoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios/{usuarioId}/permisos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class UsuarioPermisoController {

    private final UsuarioPermisoService usuarioPermisoService;

    @PostMapping
    public ResponseEntity<List<PermisoResponse>> asignar(@PathVariable Long usuarioId,
                                                         @Valid @RequestBody PermisoAsignacionRequest request) {
        Long adminId = SecurityUtils.getCurrentUserId();
        Long tenantId = SecurityUtils.getCurrentTenantId();
        List<PermisoResponse> result = usuarioPermisoService.asignarPermisos(adminId, tenantId, usuarioId, request.getPermisoIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{permisoId}")
    public ResponseEntity<Void> revocar(@PathVariable Long usuarioId, @PathVariable Long permisoId) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        usuarioPermisoService.revocarPermiso(tenantId, usuarioId, permisoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PermisoResponse>> listarDirectos(@PathVariable Long usuarioId) {
        // valida tenant internamente vía listar? listarPermisosDirectos no valida tenant, pero listarEfectivos sí.
        // Para directos, validamos leyendo usuario y comparando tenantId
        Long tenantId = SecurityUtils.getCurrentTenantId();
        // reutiliza validación de efectivos para chequear tenant antes de listar directos
        usuarioPermisoService.listarPermisosEfectivos(usuarioId, tenantId);
        return ResponseEntity.ok(usuarioPermisoService.listarPermisosDirectos(usuarioId));
    }

    @GetMapping("/efectivos")
    public ResponseEntity<List<PermisoResponse>> listarEfectivos(@PathVariable Long usuarioId) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(usuarioPermisoService.listarPermisosEfectivos(usuarioId, tenantId));
    }
}
