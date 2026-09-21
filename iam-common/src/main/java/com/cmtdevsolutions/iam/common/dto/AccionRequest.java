package com.cmtdevsolutions.iam.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccionRequest {

    @NotBlank
    @Size(max = 50)
    private String nombre;

    @Size(max = 255)
    private String descripcion;
}