package com.cmtdevsolutions.iam.common.repository;

import com.cmtdevsolutions.iam.common.entity.UsuarioPlantilla;
import com.cmtdevsolutions.iam.common.entity.UsuarioPlantillaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UsuarioPlantillaRepository extends JpaRepository<UsuarioPlantilla, UsuarioPlantillaId> {

    @Query("SELECT up FROM UsuarioPlantilla up WHERE up.usuario.id = :usuarioId")
    List<UsuarioPlantilla> findByUsuarioId(Long usuarioId);

    @Query("SELECT up FROM UsuarioPlantilla up JOIN FETCH up.plantilla p WHERE up.usuario.id = :usuarioId AND p.activa = true")
    List<UsuarioPlantilla> findByUsuarioIdWithActivePlantillas(Long usuarioId);

    @Query("SELECT up FROM UsuarioPlantilla up WHERE up.usuario.id = :usuarioId AND up.plantilla.id IN :plantillaIds")
    List<UsuarioPlantilla> findByUsuarioIdAndPlantillaIds(Long usuarioId, Set<Long> plantillaIds);

    boolean existsByUsuarioIdAndPlantillaId(Long usuarioId, Long plantillaId);

    void deleteByUsuarioId(Long usuarioId);
}