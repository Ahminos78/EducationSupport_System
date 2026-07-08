package com.whut.common.util;

import com.whut.common.auth.AuthUser;
import com.whut.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    @Value("${edu.jwt.secret}")
    private String secret;

    @Value("${edu.jwt.expire-seconds:86400}")
    private long expireSeconds;

    public String generateToken(Long userId, String username, Integer role) {
        long expiresAt = Instant.now().getEpochSecond() + expireSeconds;
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = "{" +
                "\"userId\":" + userId + "," +
                "\"username\":\"" + escape(username) + "\"," +
                "\"role\":" + role + "," +
                "\"exp\":" + expiresAt +
                "}";
        String headerPart = encode(header);
        String payloadPart = encode(payload);
        String signaturePart = sign(headerPart + "." + payloadPart);
        return headerPart + "." + payloadPart + "." + signaturePart;
    }

    public AuthUser parseToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw BusinessException.unauthorized("Token 格式错误");
        }
        String expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!constantTimeEquals(expectedSignature, parts[2])) {
            throw BusinessException.unauthorized("Token 签名无效");
        }
        Map<String, String> payload = parseFlatJson(new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8));
        long exp = Long.parseLong(payload.get("exp"));
        if (Instant.now().getEpochSecond() > exp) {
            throw BusinessException.unauthorized("Token 已过期");
        }
        return new AuthUser(
                Long.parseLong(payload.get("userId")),
                payload.get("username"),
                Integer.parseInt(payload.get("role"))
        );
    }

    private String encode(String json) {
        return URL_ENCODER.encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT 签名失败", exception);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
        byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
        if (leftBytes.length != rightBytes.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < leftBytes.length; i++) {
            result |= leftBytes[i] ^ rightBytes[i];
        }
        return result == 0;
    }

    private Map<String, String> parseFlatJson(String json) {
        String content = json.trim();
        if (content.startsWith("{")) {
            content = content.substring(1);
        }
        if (content.endsWith("}")) {
            content = content.substring(0, content.length() - 1);
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (String item : content.split(",")) {
            String[] pair = item.split(":", 2);
            String key = unquote(pair[0].trim());
            String value = unquote(pair[1].trim());
            result.put(key, value);
        }
        return result;
    }

    private String unquote(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return value;
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
