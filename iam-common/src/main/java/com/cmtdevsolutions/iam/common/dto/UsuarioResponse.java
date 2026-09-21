package com.cmtdevsolutions.iam.common.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {

    private Long id;
    private String email;
    private String nombre;
    private Boolean esAdmin;
    private Boolean activo;
    private Instant createdAt;
    private Instant updatedAt;
    private List<PermisoResponse> permisosDirectos;
    private List<PlantillaResponse> plantillasAsignadas;
}