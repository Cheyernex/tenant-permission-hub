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
public class UsuarioPermisoId implements Serializable {

    @jakarta.persistence.Column(name = "usuario_id")
    private Long usuarioId;

    @jakarta.persistence.Column(name = "permiso_id")
    private Long permisoId;
}