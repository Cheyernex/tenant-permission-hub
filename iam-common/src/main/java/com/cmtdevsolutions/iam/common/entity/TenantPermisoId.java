package com.cmtdevsolutions.iam.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @EqualsAndHashCode
public class TenantPermisoId implements Serializable {
    @Column(name = "tenant_id")
    private Long tenantId;
    @Column(name = "permiso_id")
    private Long permisoId;
}
