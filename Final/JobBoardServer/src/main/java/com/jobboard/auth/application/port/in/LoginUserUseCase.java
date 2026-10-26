package com.jobboard.auth.application.port.in;

/**
 * Inbound port for authenticating existing users.
 */
public interface LoginUserUseCase {

    RegisterUserUseCase.AuthResult login(LoginCommand command);

    record LoginCommand(String email, String password) {}
}
