package com.cmtdevsolutions.iam.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "usuario_permiso",
    indexes = {
        @Index(name = "idx_usuario_permiso_usuario", columnList = "usuario_id"),
        @Index(name = "idx_usuario_permiso_permiso", columnList = "permiso_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"usuario", "permiso"})
public class UsuarioPermiso {

    @EmbeddedId
    private UsuarioPermisoId id = new UsuarioPermisoId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_usuario_permiso_usuario"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("permisoId")
    @JoinColumn(name = "permiso_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_usuario_permiso_permiso"))
    private Permiso permiso;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}