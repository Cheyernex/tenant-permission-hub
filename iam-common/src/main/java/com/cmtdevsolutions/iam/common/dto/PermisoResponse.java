package com.cmtdevsolutions.iam.common.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermisoResponse {

    private Long id;
    private String key; // submodulo:accion
    private Long submoduloId;
    private String submoduloNombre;
    private Long accionId;
    private String accionNombre;
    private Instant createdAt;
}