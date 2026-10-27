package com.jobboard.auth.infrastructure.web;

import com.jobboard.auth.application.port.in.AuthenticateTokenUseCase;
import com.jobboard.auth.application.port.in.LoginUserUseCase;
import com.jobboard.auth.application.port.in.RegisterUserUseCase;
import com.jobboard.auth.application.port.in.UpdateUserProfileUseCase;
import com.jobboard.auth.infrastructure.web.dto.AuthResponseDto;
import com.jobboard.auth.infrastructure.web.dto.LoginRequestDto;
import com.jobboard.auth.infrastructure.web.dto.RegisterRequestDto;
import com.jobboard.auth.infrastructure.web.dto.UpdateProfileRequestDto;
import com.jobboard.auth.infrastructure.web.dto.UserResponseDto;
import com.jobboard.auth.infrastructure.web.mapper.AuthWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing user registration, login, profile lookup, and profile update endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Felhasználói regisztráció, belépés, munkamenet- és profilkezelés")
public class AuthRestController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final AuthenticateTokenUseCase authenticateTokenUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final AuthWebMapper mapper;

    public AuthRestController(RegisterUserUseCase registerUserUseCase,
                              LoginUserUseCase loginUserUseCase,
                              AuthenticateTokenUseCase authenticateTokenUseCase,
                              UpdateUserProfileUseCase updateUserProfileUseCase,
                              AuthWebMapper mapper) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.authenticateTokenUseCase = authenticateTokenUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Új felhasználó regisztrálása", description = "Regisztrál egy álláskeresőt (APPLICANT) vagy cégvezetőt (COMPANY_MANAGER).")
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        RegisterUserUseCase.RegisterCommand command = mapper.toRegisterCommand(request);
        RegisterUserUseCase.AuthResult result = registerUserUseCase.register(command);
        return mapper.toAuthResponseDto(result);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Bejelentkezés", description = "Bejelentkezteti a felhasználót érvényes e-mail és jelszó párossal, és JWT tokent ad vissza.")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        LoginUserUseCase.LoginCommand command = mapper.toLoginCommand(request);
        RegisterUserUseCase.AuthResult result = loginUserUseCase.login(command);
        return mapper.toAuthResponseDto(result);
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Aktuális felhasználó lekérdezése", description = "Visszaadja a Bearer tokenhez tartozó bejelentkezett felhasználó adatait.")
    public UserResponseDto getCurrentUser(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        AuthenticateTokenUseCase.AuthenticatedUser user = authenticateTokenUseCase.authenticate(authHeader);
        return mapper.toUserResponseDto(user);
    }

    @PutMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Felhasználói profil módosítása", description = "Módosítja a bejelentkezett felhasználó adatait (név, e-mail, cégnév, szerepkör, opcionális jelszócsere).")
    public AuthResponseDto updateProfile(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody UpdateProfileRequestDto request
    ) {
        AuthenticateTokenUseCase.AuthenticatedUser authUser = authenticateTokenUseCase.authenticate(authHeader);
        UpdateUserProfileUseCase.UpdateProfileCommand command = mapper.toUpdateProfileCommand(request);
        RegisterUserUseCase.AuthResult result = updateUserProfileUseCase.updateProfile(authUser.id(), command);
        return mapper.toAuthResponseDto(result);
    }

    @PatchMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Felhasználói profil részleges módosítása", description = "Módosítja a bejelentkezett felhasználó adatait.")
    public AuthResponseDto patchProfile(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody UpdateProfileRequestDto request
    ) {
        return updateProfile(authHeader, request);
    }
}
