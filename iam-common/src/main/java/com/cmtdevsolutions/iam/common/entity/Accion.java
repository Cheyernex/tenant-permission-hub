package com.cmtdevsolutions.iam.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "accion", indexes = {
    @Index(name = "idx_accion_submodulo_nombre", columnList = "submodulo_id, nombre", unique = true),
    @Index(name = "idx_accion_activo", columnList = "activo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "submodulo")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Accion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submodulo_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_accion_submodulo"))
    private Submodulo submodulo;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}