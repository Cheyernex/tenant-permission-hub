package com.cmtdevsolutions.iam.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "permiso", indexes = {
    @Index(name = "uk_permiso_submodulo_accion", columnList = "submodulo_id, accion_id", unique = true),
    @Index(name = "idx_permiso_submodulo", columnList = "submodulo_id"),
    @Index(name = "idx_permiso_accion", columnList = "accion_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"submodulo", "accion"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submodulo_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_permiso_submodulo"))
    private Submodulo submodulo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accion_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_permiso_accion"))
    private Accion accion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public String getKey() {
        return submodulo.getNombre() + ":" + accion.getNombre();
    }
}