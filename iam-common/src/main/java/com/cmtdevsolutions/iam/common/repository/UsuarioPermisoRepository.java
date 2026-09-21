package com.cmtdevsolutions.iam.common.repository;

import com.cmtdevsolutions.iam.common.entity.UsuarioPermiso;
import com.cmtdevsolutions.iam.common.entity.UsuarioPermisoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UsuarioPermisoRepository extends JpaRepository<UsuarioPermiso, UsuarioPermisoId> {

    @Query("SELECT up FROM UsuarioPermiso up WHERE up.usuario.id = :usuarioId")
    List<UsuarioPermiso> findByUsuarioId(Long usuarioId);

    @Query("SELECT up FROM UsuarioPermiso up JOIN FETCH up.permiso p JOIN FETCH p.submodulo s JOIN FETCH p.accion a WHERE up.usuario.id = :usuarioId")
    List<UsuarioPermiso> findByUsuarioIdWithDetails(Long usuarioId);

    @Query("SELECT up FROM UsuarioPermiso up WHERE up.usuario.id = :usuarioId AND up.permiso.id IN :permisoIds")
    List<UsuarioPermiso> findByUsuarioIdAndPermisoIds(Long usuarioId, Set<Long> permisoIds);

    boolean existsByUsuarioIdAndPermisoId(Long usuarioId, Long permisoId);

    void deleteByUsuarioId(Long usuarioId);
}