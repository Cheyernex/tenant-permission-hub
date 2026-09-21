package com.cmtdevsolutions.iam.common.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class PlantillaPermisoId implements Serializable {

    @jakarta.persistence.Column(name = "plantilla_id")
    private Long plantillaId;

    @jakarta.persistence.Column(name = "permiso_id")
    private Long permisoId;
}