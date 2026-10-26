package com.jobboard.auth.infrastructure.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobboard.auth.application.port.in.AuthenticateTokenUseCase;
import com.jobboard.auth.application.port.out.TokenServicePort;
import com.jobboard.auth.domain.exception.UnauthorizedAccessException;
import com.jobboard.auth.domain.model.User;
import com.jobboard.auth.domain.model.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standard HS256 JWT implementation using core Java cryptography and Jackson ObjectMapper.
 * Avoids extra third-party dependencies while providing RFC 7519 compliant tokens.
 */
@Component
public class JwtTokenAdapter implements TokenServicePort {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final byte[] secretKeyBytes;
    private final long expirationHours;

    public JwtTokenAdapter(
            ObjectMapper objectMapper,
            @Value("${jwt.secret:JobBoardSecretSigningKey2026AtLeast256BitsLongForHmacSha256!#%}") String secret,
            @Value("${jwt.expiration-hours:168}") long expirationHours
    ) {
        this.objectMapper = objectMapper;
        this.secretKeyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationHours = expirationHours;
    }

    @Override
    public String generateToken(User user) {
        try {
            // 1. Header
            Map<String, Object> header = Map.of(
                    "alg", "HS256",
                    "typ", "JWT"
            );
            String encodedHeader = encodeBase64Url(objectMapper.writeValueAsBytes(header));

            // 2. Payload
            Instant now = Instant.now();
            Instant expiresAt = now.plus(expirationHours, ChronoUnit.HOURS);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", user.getId() != null ? String.valueOf(user.getId()) : user.getEmail());
            if (user.getId() != null) {
                payload.put("userId", user.getId());
            }
            payload.put("name", user.getName());
            payload.put("email", user.getEmail());
            payload.put("role", user.getRole().name());
            if (user.getCompanyName() != null) {
                payload.put("companyName", user.getCompanyName());
            }
            payload.put("iat", now.getEpochSecond());
            payload.put("exp", expiresAt.getEpochSecond());

            String encodedPayload = encodeBase64Url(objectMapper.writeValueAsBytes(payload));

            // 3. Signature
            String signingInput = encodedHeader + "." + encodedPayload;
            byte[] signature = sign(signingInput.getBytes(StandardCharsets.UTF_8));
            String encodedSignature = encodeBase64Url(signature);

            return signingInput + "." + encodedSignature;
        } catch (Exception e) {
            throw new IllegalStateException("Hiba a JWT token generálásakor.", e);
        }
    }

    @Override
    public AuthenticateTokenUseCase.AuthenticatedUser verifyAndExtract(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedAccessException("Hiányzó vagy üres JWT token.");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new UnauthorizedAccessException("Érvénytelen JWT token formátum.");
        }

        String encodedHeader = parts[0];
        String encodedPayload = parts[1];
        String encodedSignature = parts[2];

        // 1. Verify signature
        String signingInput = encodedHeader + "." + encodedPayload;
        byte[] expectedSignature = sign(signingInput.getBytes(StandardCharsets.UTF_8));
        byte[] actualSignature;
        try {
            actualSignature = decodeBase64Url(encodedSignature);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedAccessException("Érvénytelen token aláírás kódolás.");
        }

        if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
            throw new UnauthorizedAccessException("Érvénytelen JWT token aláírás.");
        }

        // 2. Parse payload
        Map<String, Object> payload;
        try {
            byte[] payloadBytes = decodeBase64Url(encodedPayload);
            payload = objectMapper.readValue(payloadBytes, MAP_TYPE);
        } catch (Exception e) {
            throw new UnauthorizedAccessException("Nem sikerült a JWT payload feldolgozása.");
        }

        // 3. Check expiration
        Object expObj = payload.get("exp");
        if (expObj instanceof Number expNumber) {
            long expSeconds = expNumber.longValue();
            if (Instant.now().getEpochSecond() > expSeconds) {
                throw new UnauthorizedAccessException("A JWT token lejárt.");
            }
        }

        // 4. Extract user claims
        try {
            Long userId = null;
            Object userIdObj = payload.get("userId");
            if (userIdObj instanceof Number num) {
                userId = num.longValue();
            } else if (userIdObj instanceof String str && !str.isBlank()) {
                userId = Long.parseLong(str);
            } else {
                Object subObj = payload.get("sub");
                if (subObj instanceof String subStr) {
                    try {
                        userId = Long.parseLong(subStr);
                    } catch (NumberFormatException ignored) {}
                }
            }

            String name = (String) payload.get("name");
            String email = (String) payload.get("email");
            String roleStr = (String) payload.get("role");
            UserRole role = roleStr != null ? UserRole.valueOf(roleStr) : UserRole.APPLICANT;
            String companyName = (String) payload.get("companyName");

            return new AuthenticateTokenUseCase.AuthenticatedUser(userId, name, email, role, companyName);
        } catch (Exception e) {
            throw new UnauthorizedAccessException("Érvénytelen adatok a JWT tokenben: " + e.getMessage());
        }
    }

    private byte[] sign(byte[] data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secretKeyBytes, HMAC_SHA256));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException("Hiba a HMAC-SHA256 aláírás készítésekor.", e);
        }
    }

    private String encodeBase64Url(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private byte[] decodeBase64Url(String data) {
        return Base64.getUrlDecoder().decode(data);
    }
}
