package com.cmtdevsolutions.iam.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plantilla", indexes = {
    @Index(name = "uk_plantilla_tenant_nombre", columnList = "tenant_id, nombre", unique = true),
    @Index(name = "idx_plantilla_tenant_activa", columnList = "tenant_id, activa")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"tenant", "creadoPor", "permisos"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Plantilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_plantilla_tenant"))
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "activa", nullable = false)
    private Boolean activa = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creado_por", nullable = false,
        foreignKey = @ForeignKey(name = "fk_plantilla_creado_por"))
    private Usuario creadoPor;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "plantilla", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlantillaPermiso> permisos = new ArrayList<>();

    @OneToMany(mappedBy = "plantilla", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UsuarioPlantilla> usuariosAsignados = new ArrayList<>();
}