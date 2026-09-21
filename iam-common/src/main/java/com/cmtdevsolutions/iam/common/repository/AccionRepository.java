package com.cmtdevsolutions.iam.common.repository;

import com.cmtdevsolutions.iam.common.entity.Accion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccionRepository extends JpaRepository<Accion, Long> {

    @Query("SELECT a FROM Accion a WHERE a.submodulo.id = :submoduloId AND a.activo = true ORDER BY a.nombre")
    List<Accion> findBySubmoduloIdAndActivoTrue(Long submoduloId);

    @Query("SELECT a FROM Accion a WHERE a.submodulo.id = :submoduloId AND a.activo = true ORDER BY a.nombre")
    Page<Accion> findBySubmoduloIdAndActivoTrue(Long submoduloId, Pageable pageable);

    Optional<Accion> findBySubmoduloIdAndNombre(Long submoduloId, String nombre);

    boolean existsBySubmoduloIdAndNombre(Long submoduloId, String nombre);
}