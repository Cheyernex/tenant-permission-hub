package com.cmtdevsolutions.iam.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "usuario_plantilla",
    indexes = {
        @Index(name = "idx_usuario_plantilla_usuario", columnList = "usuario_id"),
        @Index(name = "idx_usuario_plantilla_plantilla", columnList = "plantilla_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"usuario", "plantilla"})
public class UsuarioPlantilla {

    @EmbeddedId
    private UsuarioPlantillaId id = new UsuarioPlantillaId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_usuario_plantilla_usuario"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("plantillaId")
    @JoinColumn(name = "plantilla_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_usuario_plantilla_plantilla"))
    private Plantilla plantilla;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}