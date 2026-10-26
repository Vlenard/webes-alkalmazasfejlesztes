package com.jobboard.jobs.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateJobStatusRequestDto(
        @NotBlank(message = "A státusz megadása kötelező (ACTIVE vagy CLOSED).")
        String status
) {}
