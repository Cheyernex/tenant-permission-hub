package com.cmtdevsolutions.iam.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submodulo", indexes = {
    @Index(name = "idx_submodulo_modulo_nombre", columnList = "modulo_id, nombre", unique = true),
    @Index(name = "idx_submodulo_activo", columnList = "activo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"modulo", "acciones"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Submodulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modulo_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_submodulo_modulo"))
    private Modulo modulo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "submodulo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Accion> acciones = new ArrayList<>();
}