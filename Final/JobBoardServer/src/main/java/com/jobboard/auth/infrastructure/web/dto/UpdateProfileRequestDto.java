package com.jobboard.auth.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for updating the user profile and credentials.
 * Role cannot be modified after registration.
 */
@Schema(description = "Felhasználói profil módosítási kérelem")
public record UpdateProfileRequestDto(
        @Schema(description = "A felhasználó teljes neve", example = "Kiss János")
        @NotBlank(message = "A név megadása kötelező.")
        String name,

        @Schema(description = "A felhasználó e-mail címe", example = "kiss.janos@example.com")
        @NotBlank(message = "Az e-mail cím megadása kötelező.")
        @Email(message = "Érvényes e-mail címet kell megadni.")
        String email,

        @Schema(description = "Cégnév (céges kezelő esetén)", example = "TechCorp Hungary")
        String companyName,

        @Schema(description = "Jelenlegi jelszó (jelszócsere esetén kötelező)", example = "Start123")
        String currentPassword,

        @Schema(description = "Új jelszó (legalább 6 karakter)", example = "NewSecret123")
        String newPassword
) {
}
