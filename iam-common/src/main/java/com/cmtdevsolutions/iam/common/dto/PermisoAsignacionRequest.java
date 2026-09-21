package com.cmtdevsolutions.iam.common.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermisoAsignacionRequest {

    @NotEmpty(message = "Debe proveer al menos un permiso")
    private Set<Long> permisoIds;
}
