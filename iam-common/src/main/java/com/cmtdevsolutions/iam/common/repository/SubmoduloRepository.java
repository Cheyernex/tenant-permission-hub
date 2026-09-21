package com.cmtdevsolutions.iam.common.repository;

import com.cmtdevsolutions.iam.common.entity.Submodulo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmoduloRepository extends JpaRepository<Submodulo, Long> {

    @Query("SELECT s FROM Submodulo s WHERE s.modulo.id = :moduloId AND s.activo = true ORDER BY s.orden, s.nombre")
    List<Submodulo> findByModuloIdAndActivoTrue(Long moduloId);

    @Query("SELECT s FROM Submodulo s WHERE s.modulo.id = :moduloId AND s.activo = true ORDER BY s.orden, s.nombre")
    Page<Submodulo> findByModuloIdAndActivoTrue(Long moduloId, Pageable pageable);

    @Query("SELECT s FROM Submodulo s LEFT JOIN FETCH s.acciones a WHERE s.id = :id")
    Optional<Submodulo> findByIdWithAcciones(Long id);

    Optional<Submodulo> findByModuloIdAndNombre(Long moduloId, String nombre);

    boolean existsByModuloIdAndNombre(Long moduloId, String nombre);
}