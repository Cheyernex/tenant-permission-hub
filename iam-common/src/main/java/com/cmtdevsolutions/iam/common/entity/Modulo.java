package com.cmtdevsolutions.iam.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modulo", indexes = {
    @Index(name = "idx_modulo_nombre", columnList = "nombre", unique = true),
    @Index(name = "idx_modulo_activo", columnList = "activo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "submodulos")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
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

    @OneToMany(mappedBy = "modulo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Submodulo> submodulos = new ArrayList<>();
}