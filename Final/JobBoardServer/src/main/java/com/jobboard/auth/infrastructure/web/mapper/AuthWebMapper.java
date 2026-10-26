package com.jobboard.auth.infrastructure.web.mapper;

import com.jobboard.auth.application.port.in.AuthenticateTokenUseCase;
import com.jobboard.auth.application.port.in.LoginUserUseCase;
import com.jobboard.auth.application.port.in.RegisterUserUseCase;
import com.jobboard.auth.domain.model.User;
import com.jobboard.auth.infrastructure.web.dto.AuthResponseDto;
import com.jobboard.auth.infrastructure.web.dto.LoginRequestDto;
import com.jobboard.auth.infrastructure.web.dto.RegisterRequestDto;
import com.jobboard.auth.infrastructure.web.dto.UserResponseDto;
import org.springframework.stereotype.Component;

/**
 * Maps between web DTOs and application commands/results for authentication.
 */
@Component
public class AuthWebMapper {

    public RegisterUserUseCase.RegisterCommand toRegisterCommand(RegisterRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return new RegisterUserUseCase.RegisterCommand(
                dto.name(),
                dto.email(),
                dto.password(),
                dto.role(),
                dto.companyName()
        );
    }

    public LoginUserUseCase.LoginCommand toLoginCommand(LoginRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return new LoginUserUseCase.LoginCommand(
                dto.email(),
                dto.password()
        );
    }

    public UserResponseDto toUserResponseDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCompanyName()
        );
    }

    public UserResponseDto toUserResponseDto(AuthenticateTokenUseCase.AuthenticatedUser user) {
        if (user == null) {
            return null;
        }
        return new UserResponseDto(
                user.id(),
                user.name(),
                user.email(),
                user.role(),
                user.companyName()
        );
    }

    public AuthResponseDto toAuthResponseDto(RegisterUserUseCase.AuthResult authResult) {
        if (authResult == null) {
            return null;
        }
        return new AuthResponseDto(
                authResult.token(),
                toUserResponseDto(authResult.user())
        );
    }
}
