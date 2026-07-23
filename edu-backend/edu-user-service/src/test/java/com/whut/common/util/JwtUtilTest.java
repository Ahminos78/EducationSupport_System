package com.whut.common.util;

import com.whut.common.auth.AuthUser;
import com.whut.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        setField("secret", "test-secret-key-for-jwt-unit-testing-here");
        setField("expireSeconds", 86400L);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = JwtUtil.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(jwtUtil, value);
    }

    @Test
    void generateAndParseToken_shouldWork() {
        String token = jwtUtil.generateToken(1L, "testuser", 1);
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);

        AuthUser user = jwtUtil.parseToken(token);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals(1, user.getRole());
    }

    @Test
    void generateAndParseToken_withChineseUsername() {
        String token = jwtUtil.generateToken(2L, "\u6d4b\u8bd5\u7528\u6237", 2);
        assertNotNull(token);

        AuthUser user = jwtUtil.parseToken(token);
        assertEquals(2L, user.getId());
        assertEquals("\u6d4b\u8bd5\u7528\u6237", user.getUsername());
        assertEquals(2, user.getRole());
    }

    @Test
    void generateAndParseResetToken_shouldWork() {
        String token = jwtUtil.generateResetToken(1L);
        assertNotNull(token);

        Long userId = jwtUtil.parseResetToken(token);
        assertEquals(1L, userId);
    }

    @Test
    void parseToken_shouldThrowWhenInvalid() {
        assertThrows(BusinessException.class, () -> jwtUtil.parseToken("invalid-token-format"));
        assertThrows(BusinessException.class, () -> jwtUtil.parseToken("a.b.c"));
    }

    @Test
    void parseToken_shouldThrowWhenTampered() {
        String token = jwtUtil.generateToken(1L, "testuser", 1);
        // Tamper with the signature part
        String tampered = token.substring(0, token.lastIndexOf('.') + 1) + "tampered";
        assertThrows(BusinessException.class, () -> jwtUtil.parseToken(tampered));
    }
}
