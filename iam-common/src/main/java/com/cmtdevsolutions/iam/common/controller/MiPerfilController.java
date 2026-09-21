package com.cmtdevsolutions.iam.common.controller;

import com.cmtdevsolutions.iam.common.dto.PermisoResponse;
import com.cmtdevsolutions.iam.common.security.SecurityUtils;
import com.cmtdevsolutions.iam.common.service.PermissionCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mi-perfil")
@RequiredArgsConstructor
public class MiPerfilController {

    private final PermissionCacheService cacheService;

    @GetMapping("/permisos-efectivos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PermisoResponse>> misPermisosEfectivos() {
        Long userId = SecurityUtils.getCurrentUserId();
        Long tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(cacheService.getEffectivePermisosCached(tenantId, userId));
    }
}
