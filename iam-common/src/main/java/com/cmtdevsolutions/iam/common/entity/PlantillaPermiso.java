package com.cmtdevsolutions.iam.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "plantilla_permiso",
    indexes = {
        @Index(name = "idx_plantilla_permiso_plantilla", columnList = "plantilla_id"),
        @Index(name = "idx_plantilla_permiso_permiso", columnList = "permiso_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"plantilla", "permiso"})
public class PlantillaPermiso {

    @EmbeddedId
    private PlantillaPermisoId id = new PlantillaPermisoId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("plantillaId")
    @JoinColumn(name = "plantilla_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_plantilla_permiso_plantilla"))
    private Plantilla plantilla;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("permisoId")
    @JoinColumn(name = "permiso_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_plantilla_permiso_permiso"))
    private Permiso permiso;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}