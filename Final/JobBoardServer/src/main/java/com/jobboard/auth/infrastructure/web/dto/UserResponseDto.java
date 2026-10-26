package com.jobboard.auth.infrastructure.web.dto;

import com.jobboard.auth.domain.model.UserRole;

/**
 * Response payload representing public user information.
 */
public record UserResponseDto(
        Long id,
        String name,
        String email,
        UserRole role,
        String companyName
) {
}
