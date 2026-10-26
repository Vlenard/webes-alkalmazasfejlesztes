package com.jobboard.auth.infrastructure.security;

import com.jobboard.auth.application.port.out.PasswordEncoderPort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * SHA-256 based implementation of PasswordEncoderPort.
 */
@Component
public class Sha256PasswordEncoderAdapter implements PasswordEncoderPort {

    private static final String SALT = "JobBoardAuthSalt_v1#";

    @Override
    public String encode(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("A jelszó nem lehet null.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((SALT + rawPassword).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algoritmus nem elérhető a futtatókörnyezetben.", e);
        }
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return encode(rawPassword).equalsIgnoreCase(encodedPassword);
    }
}
