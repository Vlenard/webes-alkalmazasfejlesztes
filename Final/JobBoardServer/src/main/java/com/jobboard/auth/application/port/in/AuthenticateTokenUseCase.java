package com.jobboard.auth.application.port.in;

import com.jobboard.auth.domain.model.UserRole;

/**
 * Inbound port for verifying JWT tokens and resolving the current authenticated user context.
 */
public interface AuthenticateTokenUseCase {

    AuthenticatedUser authenticate(String authorizationHeader);

    AuthenticatedUser authenticateOptional(String authorizationHeader);

    record AuthenticatedUser(Long id, String name, String email, UserRole role, String companyName) {
        public boolean isCompanyManager() {
            return role == UserRole.COMPANY_MANAGER;
        }

        public boolean isApplicant() {
            return role == UserRole.APPLICANT;
        }
    }
}
