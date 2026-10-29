package com.jobboard.auth.infrastructure.web.dto;

import com.jobboard.auth.domain.model.UserRole;
import java.util.List;

/**
 * Response payload representing public user information.
 */
public record UserResponseDto(
        Long id,
        String name,
        String email,
        UserRole role,
        String companyName,
        List<String> skills
) {
    public UserResponseDto(Long id, String name, String email, UserRole role, String companyName) {
        this(id, name, email, role, companyName, List.of());
    }
}
