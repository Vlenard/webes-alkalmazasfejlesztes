package com.jobboard.auth.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for user login.
 */
public record LoginRequestDto(
        @NotBlank(message = "Az e-mail cím megadása kötelező.")
        @Email(message = "Érvénytelen e-mail formátum.")
        String email,

        @NotBlank(message = "A jelszó megadása kötelező.")
        String password
) {
}
