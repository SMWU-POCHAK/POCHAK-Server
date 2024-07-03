package com.apps.pochak.login.util;

import com.apps.pochak.global.api_payload.exception.handler.AppleOAuthException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.JSON_PROCESSING_EXCEPTION;

@Component
@RequiredArgsConstructor
public class JwtValidator {

    public Map<String, String> parseHeaders(String token) {
        try {
            String header = token.split("\\.")[0];
            return new ObjectMapper().readValue(decodeHeader(header), Map.class);
        } catch (JsonProcessingException e) {
            throw new AppleOAuthException(JSON_PROCESSING_EXCEPTION);
        }
    }

    public String decodeHeader(String token) {
        return new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
    }

    public Claims getTokenClaims(String token, PublicKey publicKey) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
