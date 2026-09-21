package com.cmtdevsolutions.iam.common.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermisoAsignacionResponse {

    private Long usuarioId;
    private List<PermisoResponse> permisosAsignados;
    private List<PermisoResponse> permisosRevocados;
}
