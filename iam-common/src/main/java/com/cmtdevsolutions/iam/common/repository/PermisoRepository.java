package com.cmtdevsolutions.iam.common.repository;

import com.cmtdevsolutions.iam.common.entity.Permiso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    Optional<Permiso> findBySubmoduloIdAndAccionId(Long submoduloId, Long accionId);

    @Query("SELECT p FROM Permiso p JOIN FETCH p.submodulo s JOIN FETCH p.accion a WHERE p.id IN :ids")
    List<Permiso> findAllByIdWithDetails(Set<Long> ids);

    @Query("SELECT p FROM Permiso p JOIN FETCH p.submodulo s JOIN FETCH p.accion a WHERE s.activo = true AND a.activo = true ORDER BY s.nombre, a.nombre")
    List<Permiso> findAllActivosWithDetails();

    @Query("SELECT p FROM Permiso p JOIN FETCH p.submodulo s JOIN FETCH p.accion a WHERE s.activo = true AND a.activo = true ORDER BY s.nombre, a.nombre")
    Page<Permiso> findAllActivosWithDetails(Pageable pageable);

    @Query("SELECT p FROM Permiso p JOIN FETCH p.submodulo s JOIN FETCH p.accion a WHERE p.submodulo.id = :submoduloId AND s.activo = true AND a.activo = true ORDER BY a.nombre")
    List<Permiso> findBySubmoduloIdWithDetails(Long submoduloId);

    boolean existsBySubmoduloIdAndAccionId(Long submoduloId, Long accionId);
}