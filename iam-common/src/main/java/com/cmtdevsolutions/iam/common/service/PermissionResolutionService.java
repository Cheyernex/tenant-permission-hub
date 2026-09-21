package com.cmtdevsolutions.iam.common.service;

import com.cmtdevsolutions.iam.common.dto.PermisoResponse;
import com.cmtdevsolutions.iam.common.entity.Permiso;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionResolutionService {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Resuelve permisos efectivos de un usuario: unión de directos + vía plantillas activas.
     * Usa query única con UNION para evitar N+1.
     */
    @SuppressWarnings("unchecked")
    public Set<Long> getEffectivePermisoIds(Long usuarioId) {
        List<Number> rows = entityManager.createNativeQuery(
                "SELECT permiso_id FROM (" +
                "  SELECT up.permiso_id FROM usuario_permiso up WHERE up.usuario_id = :uid " +
                "  UNION " +
                "  SELECT pp.permiso_id FROM usuario_plantilla upl " +
                "  JOIN plantilla p ON p.id = upl.plantilla_id AND p.activa = true " +
                "  JOIN plantilla_permiso pp ON pp.plantilla_id = p.id " +
                "  WHERE upl.usuario_id = :uid" +
                ") u"
        ).setParameter("uid", usuarioId).getResultList();
        return rows.stream().map(Number::longValue).collect(Collectors.toSet());
    }

    public Set<String> getEffectivePermissionKeys(Long usuarioId) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(
                "SELECT s.nombre, a.nombre FROM (" +
                "  SELECT up.permiso_id FROM usuario_permiso up WHERE up.usuario_id = :uid " +
                "  UNION " +
                "  SELECT pp.permiso_id FROM usuario_plantilla upl " +
                "  JOIN plantilla p ON p.id = upl.plantilla_id AND p.activa = true " +
                "  JOIN plantilla_permiso pp ON pp.plantilla_id = p.id " +
                "  WHERE upl.usuario_id = :uid" +
                ") u " +
                "JOIN permiso perm ON perm.id = u.permiso_id " +
                "JOIN submodulo s ON s.id = perm.submodulo_id " +
                "JOIN accion a ON a.id = perm.accion_id"
        ).setParameter("uid", usuarioId).getResultList();
        return rows.stream().map(r -> r[0] + ":" + r[1]).collect(Collectors.toSet());
    }

    /**
     * Lista detallada de permisos efectivos con join a submodulo/accion para response DTO.
     * Usa DISTINCT para deduplicar permiso que viene por vía directa y plantilla a la vez.
     */
    @SuppressWarnings("unchecked")
    public List<PermisoResponse> getEffectivePermisos(Long usuarioId) {
        List<Object[]> rows = entityManager.createNativeQuery(
                "SELECT DISTINCT perm.id, s.id, s.nombre, a.id, a.nombre, perm.created_at " +
                "FROM (" +
                "  SELECT up.permiso_id FROM usuario_permiso up WHERE up.usuario_id = :uid " +
                "  UNION " +
                "  SELECT pp.permiso_id FROM usuario_plantilla upl " +
                "  JOIN plantilla p ON p.id = upl.plantilla_id AND p.activa = true " +
                "  JOIN plantilla_permiso pp ON pp.plantilla_id = p.id " +
                "  WHERE upl.usuario_id = :uid" +
                ") u " +
                "JOIN permiso perm ON perm.id = u.permiso_id " +
                "JOIN submodulo s ON s.id = perm.submodulo_id " +
                "JOIN accion a ON a.id = perm.accion_id " +
                "ORDER BY s.nombre, a.nombre"
        ).setParameter("uid", usuarioId).getResultList();

        return rows.stream().map(r -> PermisoResponse.builder()
                .id(((Number) r[0]).longValue())
                .submoduloId(((Number) r[1]).longValue())
                .submoduloNombre((String) r[2])
                .accionId(((Number) r[3]).longValue())
                .accionNombre((String) r[4])
                .key(r[2] + ":" + r[4])
                .createdAt(r[5] != null ? ((java.sql.Timestamp) r[5]).toInstant() : null)
                .build()).collect(Collectors.toList());
    }
}
