package com.jobboard.applications.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO matching the frontend submitApplication argument structure.
 */
public record SubmitApplicationRequestDto(
        @NotNull(message = "A munka azonosítója (jobId) kötelező.")
        @Positive(message = "A munka azonosítója pozitív szám kell legyen.")
        Long jobId,

        @NotBlank(message = "A teljes név megadása kötelező.")
        String applicantName,

        @NotBlank(message = "Az email cím megadása kötelező.")
        @Email(message = "Kérjük, adj meg egy érvényes email címet!")
        String applicantEmail,

        String resumeFileName
) {}
