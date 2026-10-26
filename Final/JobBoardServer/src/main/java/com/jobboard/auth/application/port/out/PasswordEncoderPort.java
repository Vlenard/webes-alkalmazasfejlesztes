package com.jobboard.auth.application.port.out;

/**
 * Outbound port for hashing and matching passwords.
 */
public interface PasswordEncoderPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
