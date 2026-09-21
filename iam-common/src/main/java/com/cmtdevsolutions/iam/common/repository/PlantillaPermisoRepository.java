package com.cmtdevsolutions.iam.common.repository;

import com.cmtdevsolutions.iam.common.entity.PlantillaPermiso;
import com.cmtdevsolutions.iam.common.entity.PlantillaPermisoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PlantillaPermisoRepository extends JpaRepository<PlantillaPermiso, PlantillaPermisoId> {

    @Query("SELECT pp FROM PlantillaPermiso pp WHERE pp.plantilla.id = :plantillaId")
    List<PlantillaPermiso> findByPlantillaId(Long plantillaId);

    @Query("SELECT pp FROM PlantillaPermiso pp WHERE pp.plantilla.id = :plantillaId AND pp.permiso.id IN :permisoIds")
    List<PlantillaPermiso> findByPlantillaIdAndPermisoIds(Long plantillaId, Set<Long> permisoIds);

    boolean existsByPlantillaIdAndPermisoId(Long plantillaId, Long permisoId);

    void deleteByPlantillaId(Long plantillaId);
}