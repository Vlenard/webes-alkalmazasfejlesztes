package com.jobboard.auth.infrastructure.web.dto;

import com.jobboard.auth.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for registering a new user.
 */
public record RegisterRequestDto(
        @NotBlank(message = "A név megadása kötelező.")
        String name,

        @NotBlank(message = "Az e-mail cím megadása kötelező.")
        @Email(message = "Érvénytelen e-mail formátum.")
        String email,

        @NotBlank(message = "A jelszó megadása kötelező.")
        @Size(min = 6, message = "A jelszónak legalább 6 karakterből kell állnia.")
        String password,

        @NotNull(message = "A szerepkör kiválasztása kötelező.")
        UserRole role,

        String companyName
) {
}
