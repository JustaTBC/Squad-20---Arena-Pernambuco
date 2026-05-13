package com.arenapernambuco.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsernameEncodingTest {

    @Test
    void encode_decode_emailSimples_roundtrip() {
        String original = "participante@arena.com";
        assertEquals(original, IngressoFirebaseRepository.decodeUsername(
                IngressoFirebaseRepository.encodeUsername(original)));
    }

    @Test
    void encode_decode_emailComUnderscore_roundtrip() {
        String original = "user_name@example.com";
        assertEquals(original, IngressoFirebaseRepository.decodeUsername(
                IngressoFirebaseRepository.encodeUsername(original)));
    }

    @Test
    void encode_decode_emailComMultiplosUnderscores_roundtrip() {
        String original = "first_last_123@my_domain.com";
        assertEquals(original, IngressoFirebaseRepository.decodeUsername(
                IngressoFirebaseRepository.encodeUsername(original)));
    }

    @Test
    void encode_naoContemPonto() {
        String encoded = IngressoFirebaseRepository.encodeUsername("user.name@domain.com");
        assertEquals(false, encoded.contains("."));
    }

    @Test
    void encodeUsername_emailSemUnderscore_mantémCompatibilidadeVisual() {
        String encoded = IngressoFirebaseRepository.encodeUsername("admin@arena.com");
        assertEquals("admin_2arena_1com", encoded);
    }
}
