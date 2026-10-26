package com.jobboard.auth.application.service;

import com.jobboard.auth.application.port.in.AuthenticateTokenUseCase;
import com.jobboard.auth.application.port.in.LoginUserUseCase;
import com.jobboard.auth.application.port.in.RegisterUserUseCase;
import com.jobboard.auth.application.port.out.PasswordEncoderPort;
import com.jobboard.auth.application.port.out.TokenServicePort;
import com.jobboard.auth.application.port.out.UserRepositoryPort;
import com.jobboard.auth.domain.exception.InvalidCredentialsException;
import com.jobboard.auth.domain.exception.InvalidUserDataException;
import com.jobboard.auth.domain.exception.UnauthorizedAccessException;
import com.jobboard.auth.domain.exception.UserAlreadyExistsException;
import com.jobboard.auth.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service implementing user authentication, registration, and token validation use cases.
 */
@Service
@Transactional
public class AuthService implements RegisterUserUseCase, LoginUserUseCase, AuthenticateTokenUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenServicePort tokenServicePort;

    public AuthService(UserRepositoryPort userRepositoryPort,
                       PasswordEncoderPort passwordEncoderPort,
                       TokenServicePort tokenServicePort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.tokenServicePort = tokenServicePort;
    }

    @Override
    public AuthResult register(RegisterCommand command) {
        if (command == null) {
            throw new InvalidUserDataException("A regisztrációs adatok nem lehetnek üresek.");
        }
        if (command.password() == null || command.password().isBlank()) {
            throw new InvalidUserDataException("A jelszó megadása kötelező.");
        }
        if (command.password().length() < 6) {
            throw new InvalidUserDataException("A jelszónak legalább 6 karakterből kell állnia.");
        }

        String normalizedEmail = command.email() != null ? command.email().trim().toLowerCase() : "";
        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
            throw new InvalidUserDataException("Érvényes e-mail cím megadása kötelező.");
        }

        if (userRepositoryPort.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("A megadott e-mail címmel már regisztráltak: " + normalizedEmail);
        }

        String passwordHash = passwordEncoderPort.encode(command.password());
        User user = User.createNew(
                command.name(),
                normalizedEmail,
                passwordHash,
                command.role(),
                command.companyName()
        );

        User savedUser = userRepositoryPort.save(user);
        String token = tokenServicePort.generateToken(savedUser);

        return new AuthResult(token, savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResult login(LoginCommand command) {
        if (command == null || command.email() == null || command.email().isBlank()
                || command.password() == null || command.password().isBlank()) {
            throw new InvalidCredentialsException("Hibás e-mail cím vagy jelszó.");
        }

        String normalizedEmail = command.email().trim().toLowerCase();
        User user = userRepositoryPort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Hibás e-mail cím vagy jelszó."));

        if (!passwordEncoderPort.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Hibás e-mail cím vagy jelszó.");
        }

        String token = tokenServicePort.generateToken(user);
        return new AuthResult(token, user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedUser authenticate(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedAccessException("Hiányzó vagy érvénytelen Authorization fejléc.");
        }

        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new UnauthorizedAccessException("Üres JWT token.");
        }

        return tokenServicePort.verifyAndExtract(token);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedUser authenticateOptional(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            return null;
        }

        try {
            return tokenServicePort.verifyAndExtract(token);
        } catch (Exception e) {
            return null;
        }
    }
}
