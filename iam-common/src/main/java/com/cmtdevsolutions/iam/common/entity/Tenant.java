package com.cmtdevsolutions.iam.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tenant", indexes = {
    @Index(name = "uk_tenant_nombre", columnList = "nombre", unique = true),
    @Index(name = "uk_tenant_codigo", columnList = "codigo", unique = true),
    @Index(name = "idx_tenant_activo", columnList = "activo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "usuarios")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String nombre;

    @Column(nullable = false, length = 50, unique = true)
    private String codigo;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Usuario> usuarios = new ArrayList<>();

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Plantilla> plantillas = new ArrayList<>();
}