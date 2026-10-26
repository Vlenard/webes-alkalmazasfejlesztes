package com.jobboard.auth.application.port.out;

import com.jobboard.auth.application.port.in.AuthenticateTokenUseCase;
import com.jobboard.auth.domain.model.User;

/**
 * Outbound port for generating and validating authentication tokens.
 */
public interface TokenServicePort {

    String generateToken(User user);

    AuthenticateTokenUseCase.AuthenticatedUser verifyAndExtract(String token);
}
