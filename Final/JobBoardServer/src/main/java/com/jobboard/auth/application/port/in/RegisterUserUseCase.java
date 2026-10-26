package com.jobboard.auth.application.port.in;

import com.jobboard.auth.domain.model.User;
import com.jobboard.auth.domain.model.UserRole;

/**
 * Inbound port for registering new applicants and company managers.
 */
public interface RegisterUserUseCase {

    AuthResult register(RegisterCommand command);

    record RegisterCommand(String name, String email, String password, UserRole role, String companyName) {}

    record AuthResult(String token, User user) {}
}
