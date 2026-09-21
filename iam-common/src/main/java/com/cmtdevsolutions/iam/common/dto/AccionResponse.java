package com.cmtdevsolutions.iam.common.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccionResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private Instant createdAt;
    private Instant updatedAt;
}