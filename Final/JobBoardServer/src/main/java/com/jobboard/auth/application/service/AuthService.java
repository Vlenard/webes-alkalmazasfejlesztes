package com.jobboard.auth.application.service;

import com.jobboard.auth.application.port.in.AuthenticateTokenUseCase;
import com.jobboard.auth.application.port.in.LoginUserUseCase;
import com.jobboard.auth.application.port.in.RegisterUserUseCase;
import com.jobboard.auth.application.port.in.UpdateUserProfileUseCase;
import com.jobboard.auth.application.port.out.PasswordEncoderPort;
import com.jobboard.auth.application.port.out.TokenServicePort;
import com.jobboard.auth.application.port.out.UserRepositoryPort;
import com.jobboard.auth.domain.exception.InvalidCredentialsException;
import com.jobboard.auth.domain.exception.InvalidUserDataException;
import com.jobboard.auth.domain.exception.UnauthorizedAccessException;
import com.jobboard.auth.domain.exception.UserAlreadyExistsException;
import com.jobboard.auth.domain.model.User;
import com.jobboard.auth.domain.model.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application service implementing user authentication, registration, profile updates, and token validation use cases.
 */
@Service
@Transactional
public class AuthService implements RegisterUserUseCase, LoginUserUseCase, AuthenticateTokenUseCase, UpdateUserProfileUseCase {

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

    @Override
    public AuthResult updateProfile(Long userId, UpdateProfileCommand command) {
        if (command == null) {
            throw new InvalidUserDataException("A profil módosítási adatai nem lehetnek üresek.");
        }
        if (userId == null) {
            throw new UnauthorizedAccessException("Érvénytelen felhasználói azonosító.");
        }

        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UnauthorizedAccessException("A felhasználó nem található."));

        // Validate and normalize name
        String updatedName = (command.name() != null) ? command.name().trim() : user.getName();
        if (updatedName.isBlank()) {
            throw new InvalidUserDataException("A név nem lehet üres.");
        }

        // Validate and check email
        String updatedEmail = (command.email() != null) ? command.email().trim().toLowerCase() : user.getEmail();
        if (updatedEmail.isBlank() || !updatedEmail.contains("@")) {
            throw new InvalidUserDataException("Érvényes e-mail cím megadása kötelező.");
        }

        if (!updatedEmail.equalsIgnoreCase(user.getEmail())) {
            Optional<User> existing = userRepositoryPort.findByEmail(updatedEmail);
            if (existing.isPresent() && !existing.get().getId().equals(userId)) {
                throw new UserAlreadyExistsException("A megadott e-mail címmel már regisztráltak: " + updatedEmail);
            }
        }

        // Handle optional password change
        String updatedPasswordHash = user.getPasswordHash();
        if (command.newPassword() != null && !command.newPassword().isBlank()) {
            if (command.currentPassword() == null || command.currentPassword().isBlank()) {
                throw new InvalidUserDataException("A jelszó módosításához meg kell adni a jelenlegi jelszót.");
            }
            if (!passwordEncoderPort.matches(command.currentPassword(), user.getPasswordHash())) {
                throw new InvalidCredentialsException("A jelenlegi jelszó helytelen.");
            }
            if (command.newPassword().length() < 6) {
                throw new InvalidUserDataException("Az új jelszónak legalább 6 karakterből kell állnia.");
            }
            updatedPasswordHash = passwordEncoderPort.encode(command.newPassword());
        }

        // Handle company name (role is strictly immutable and preserved)
        String updatedCompanyName = command.companyName() != null ? command.companyName().trim() : user.getCompanyName();

        User updatedUser = user.withUpdatedProfile(updatedName, updatedEmail, updatedCompanyName, updatedPasswordHash);
        User savedUser = userRepositoryPort.save(updatedUser);
        String token = tokenServicePort.generateToken(savedUser);

        return new AuthResult(token, savedUser);
    }
}
