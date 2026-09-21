package com.cmtdevsolutions.iam.common.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantillaResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activa;
    private Long creadoPorId;
    private String creadoPorNombre;
    private Instant createdAt;
    private Instant updatedAt;
    private List<PermisoResponse> permisos;
}