package com.apps.pochak.login.jwt;

import com.apps.pochak.global.api_payload.exception.handler.ExpiredPeriodJwtException;
import com.apps.pochak.global.api_payload.exception.handler.InvalidJwtException;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static com.apps.pochak.global.Constant.AUTHORITIES_KEY;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.*;

@Getter
@Service
@RequiredArgsConstructor
public class JwtService {
    public static final String EMPTY_SUBJECT = "";
    private final MemberRepository memberRepository;
    private final long accessTokenExpirationTime = 1000L * 60 * 60; // 1H
    private final long refreshTokenExpirationTime = 1000L * 60 * 60 * 24 * 30; // 1M
    private Key key;

    @Value("${jwt.secretKey}")
    private String secretKey;

    @PostConstruct
    private void _getSecretKey() {
        String keyBase64Encoded = Base64.getEncoder().encodeToString(secretKey.getBytes());
        key = Keys.hmacShaKeyFor(keyBase64Encoded.getBytes());
    }

    public String createAccessToken(final String subject) {
        return createToken(subject, accessTokenExpirationTime);
    }

    public String createRefreshToken() {
        return createToken(EMPTY_SUBJECT, refreshTokenExpirationTime);
    }

    private String createToken(final String subject, final Long validityInMilliseconds) {
        final Date now = new Date();
        final Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setSubject(subject)
                .setIssuedAt(now)
                .claim(AUTHORITIES_KEY, "ROLE_USER")
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateAccessToken(final String accessToken) {
        try {
            parseToken(accessToken);
            return true;
        } catch (ExpiredJwtException e) {
            throw new ExpiredPeriodJwtException(EXPIRED_ACCESS_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtException(FAIL_VALIDATE_TOKEN);
        }
    }

    private void validateRefreshToken(final String refreshToken) {
        try {
            parseToken(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new ExpiredPeriodJwtException(EXPIRED_REFRESH_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtException(FAIL_VALIDATE_TOKEN);
        }
    }

    public Claims getTokenClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public String getSubject(final String token) {
        return parseToken(token)
                .getBody()
                .getSubject();
    }

    private Jws<Claims> parseToken(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public boolean isValidRefreshAndInvalidAccess(final String refreshToken, final String accessToken) {
        validateRefreshToken(refreshToken);
        try {
            validateAccessToken(accessToken);
        } catch (final ExpiredPeriodJwtException e) {
            return true;
        }
        return false;
    }

    public boolean isValidRefreshAndValidAccess(final String refreshToken, final String accessToken) {
        try {
            validateRefreshToken(refreshToken);
            validateAccessToken(accessToken);
            return true;
        } catch (final JwtException e) {
            return false;
        }
    }

    // custom method
    // TODO: 다른 방식 찾아오기
    public Member getLoginMember() {
        final String id = getLoginMemberId();
        return memberRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new InvalidJwtException(INVALID_ACCESS_TOKEN));
    }

    public String getLoginMemberId() {
        String accessToken = JwtHeaderUtil.getAccessToken();
        return getSubject(accessToken);
    }
}
