package com.cmtdevsolutions.iam.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario", indexes = {
    @Index(name = "uk_usuario_tenant_email", columnList = "tenant_id, email", unique = true),
    @Index(name = "idx_usuario_tenant_activo", columnList = "tenant_id, activo"),
    @Index(name = "idx_usuario_admin", columnList = "tenant_id, es_admin")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"tenant", "permisosDirectos", "plantillasAsignadas"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_usuario_tenant"))
    private Tenant tenant;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "es_admin", nullable = false)
    private Boolean esAdmin = false;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UsuarioPermiso> permisosDirectos = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UsuarioPlantilla> plantillasAsignadas = new ArrayList<>();

    @OneToMany(mappedBy = "creadoPor", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Plantilla> plantillasCreadas = new ArrayList<>();
}