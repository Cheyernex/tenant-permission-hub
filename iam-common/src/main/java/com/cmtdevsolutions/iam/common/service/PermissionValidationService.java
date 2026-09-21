package com.cmtdevsolutions.iam.common.service;

import com.cmtdevsolutions.iam.common.entity.Usuario;
import com.cmtdevsolutions.iam.common.exception.PrivilegeEscalationException;
import com.cmtdevsolutions.iam.common.exception.TenantIsolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PermissionValidationService {

    private final PermissionResolutionService permissionResolutionService;

    /**
     * Valida que el usuario destino pertenezca al mismo tenant que el admin.
     * Lanza TenantIsolationException si no coincide.
     */
    public void validarMismoTenant(Long adminTenantId, Usuario targetUser) {
        if (!adminTenantId.equals(targetUser.getTenant().getId())) {
            throw new TenantIsolationException(
                    "No puedes gestionar usuarios de otro tenant (admin tenant=" + adminTenantId +
                    ", target tenant=" + targetUser.getTenant().getId() + ")");
        }
    }

    /**
     * Valida anti-escalación: el admin debe poseer efectivamente todos los permisos que intenta delegar.
     * Lanza PrivilegeEscalationException si falta alguno.
     */
    public void validarAntiEscalacion(Long adminUserId, Set<Long> permisoIdsSolicitados) {
        if (permisoIdsSolicitados == null || permisoIdsSolicitados.isEmpty()) return;
        Set<Long> efectivos = permissionResolutionService.getEffectivePermisoIds(adminUserId);
        Set<Long> faltantes = new HashSet<>(permisoIdsSolicitados);
        faltantes.removeAll(efectivos);
        if (!faltantes.isEmpty()) {
            throw new PrivilegeEscalationException(
                    "No puedes delegar permisos que no posees. Faltantes: " + faltantes);
        }
    }
}
