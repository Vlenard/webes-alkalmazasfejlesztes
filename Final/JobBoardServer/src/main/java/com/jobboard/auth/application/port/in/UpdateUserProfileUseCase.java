package com.jobboard.auth.application.port.in;

import com.jobboard.auth.application.port.in.RegisterUserUseCase.AuthResult;
import java.util.List;

/**
 * Inbound port for updating an existing user's profile and credentials.
 * Note: User role (APPLICANT / COMPANY_MANAGER) cannot be modified after registration.
 */
public interface UpdateUserProfileUseCase {

    AuthResult updateProfile(Long userId, UpdateProfileCommand command);

    record UpdateProfileCommand(
            String name,
            String email,
            String companyName,
            String currentPassword,
            String newPassword,
            List<String> skills
    ) {
        public UpdateProfileCommand(String name, String email, String companyName, String currentPassword, String newPassword) {
            this(name, email, companyName, currentPassword, newPassword, null);
        }
    }
}
