package com.jobboard.applications.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateApplicationStatusRequestDto(
        @NotBlank(message = "A státusz megadása kötelező (ACCEPTED vagy REJECTED).")
        String status
) {}
