package com.cmtdevsolutions.iam.common.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmoduloResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private Integer orden;
    private Boolean activo;
    private Instant createdAt;
    private Instant updatedAt;
    private List<AccionResponse> acciones;
}