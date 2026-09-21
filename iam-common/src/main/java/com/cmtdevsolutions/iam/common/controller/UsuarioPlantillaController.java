package com.cmtdevsolutions.iam.common.controller;

import com.cmtdevsolutions.iam.common.dto.PlantillaResponse;
import com.cmtdevsolutions.iam.common.security.SecurityUtils;
import com.cmtdevsolutions.iam.common.service.UsuarioPlantillaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios/{usuarioId}/plantillas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT_ADMIN')")
public class UsuarioPlantillaController {

    private final UsuarioPlantillaService usuarioPlantillaService;

    @PostMapping("/{plantillaId}")
    public ResponseEntity<Void> asignar(@PathVariable Long usuarioId, @PathVariable Long plantillaId) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        usuarioPlantillaService.asignarPlantilla(tenantId, usuarioId, plantillaId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{plantillaId}")
    public ResponseEntity<Void> revocar(@PathVariable Long usuarioId, @PathVariable Long plantillaId) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        usuarioPlantillaService.revocarPlantilla(tenantId, usuarioId, plantillaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PlantillaResponse>> listar(@PathVariable Long usuarioId) {
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(usuarioPlantillaService.listarPlantillasDeUsuario(tenantId, usuarioId));
    }
}
