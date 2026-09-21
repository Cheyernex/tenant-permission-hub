package com.cmtdevsolutions.iam.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "tenant_permiso", indexes = {
    @Index(name = "idx_tenant_permiso_tenant", columnList = "tenant_id"),
    @Index(name = "idx_tenant_permiso_permiso", columnList = "permiso_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"tenant","permiso"})
public class TenantPermiso {
    @EmbeddedId
    private TenantPermisoId id = new TenantPermisoId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("tenantId")
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tenant_permiso_tenant"))
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("permisoId")
    @JoinColumn(name = "permiso_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tenant_permiso_permiso"))
    private Permiso permiso;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
