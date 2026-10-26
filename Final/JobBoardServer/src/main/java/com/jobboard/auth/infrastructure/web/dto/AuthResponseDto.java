package com.jobboard.auth.infrastructure.web.dto;

/**
 * Response payload returned upon successful registration or login.
 */
public record AuthResponseDto(
        String token,
        UserResponseDto user
) {
}
