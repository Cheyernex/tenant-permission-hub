package com.cmtdevsolutions.iam.common.repository;

import com.cmtdevsolutions.iam.common.entity.Modulo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuloRepository extends JpaRepository<Modulo, Long> {

    Optional<Modulo> findByNombre(String nombre);

    @Query("SELECT m FROM Modulo m WHERE m.activo = true ORDER BY m.orden, m.nombre")
    List<Modulo> findAllActivos();

    @Query("SELECT m FROM Modulo m WHERE m.activo = true ORDER BY m.orden, m.nombre")
    Page<Modulo> findAllActivos(Pageable pageable);

    @Query("SELECT m FROM Modulo m LEFT JOIN FETCH m.submodulos s WHERE m.id = :id")
    Optional<Modulo> findByIdWithSubmodulos(Long id);

    boolean existsByNombre(String nombre);
}