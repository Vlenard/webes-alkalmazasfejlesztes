package com.jobboard.auth.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Sha256PasswordEncoderAdapterTest {

    private Sha256PasswordEncoderAdapter passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new Sha256PasswordEncoderAdapter();
    }

    @Test
    @DisplayName("Jelszó kódolása és sikeres összehasonlítása")
    void shouldEncodeAndMatchPassword() {
        String raw = "mySecurePassword123";
        String encoded = passwordEncoder.encode(raw);

        assertThat(encoded).isNotBlank();
        assertThat(encoded).isNotEqualTo(raw);
        assertThat(passwordEncoder.matches(raw, encoded)).isTrue();
        assertThat(passwordEncoder.matches("wrongPassword", encoded)).isFalse();
    }

    @Test
    @DisplayName("Null jelszó esetén kivételt dob encode híváskor")
    void shouldThrowExceptionWhenPasswordIsNull() {
        assertThatThrownBy(() -> passwordEncoder.encode(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Null paraméterek esetén matches false-t ad")
    void shouldReturnFalseForNullMatchesParameters() {
        assertThat(passwordEncoder.matches(null, "hash")).isFalse();
        assertThat(passwordEncoder.matches("password", null)).isFalse();
    }
}
