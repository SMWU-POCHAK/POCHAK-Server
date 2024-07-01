package com.apps.pochak.login.jwt;

import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.global.api_payload.exception.handler.InvalidJwtException;
import com.apps.pochak.global.api_payload.exception.handler.RefreshTokenException;
import com.apps.pochak.login.dto.response.PostTokenResponse;
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

    public String validateRefreshToken(String accessToken, String refreshToken) {
        String handle = getSubject(accessToken);
        Member member = memberRepository.findByHandleWithoutLogin(handle);

        if (member.getRefreshToken() == null || member.getRefreshToken().isEmpty())
            throw new RefreshTokenException(NULL_REFRESH_TOKEN);

        if (!member.getRefreshToken().equals(refreshToken))
            throw new RefreshTokenException(INVALID_REFRESH_TOKEN);

        return member.getHandle();
    }

    public void validate(String token) {
        try {
            parseToken(token);
        } catch (SecurityException e) {
            throw new InvalidJwtException(INVALID_TOKEN_SIGNATURE);
        } catch (MalformedJwtException e) {
            throw new InvalidJwtException(MALFORMED_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new InvalidJwtException(EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new InvalidJwtException(UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new InvalidJwtException(INVALID_TOKEN);
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
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
    }

    public PostTokenResponse reissueAccessToken() {
        String accessToken = JwtHeaderUtil.getAccessToken();
        String refreshToken = JwtHeaderUtil.getRefreshToken();

        if (refreshToken == null)
            throw new RefreshTokenException(NULL_REFRESH_TOKEN);

        validate(refreshToken);

        String handle = validateRefreshToken(accessToken, refreshToken);
        String newAccessToken = createAccessToken(handle);

        return PostTokenResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    // custom
    public Member getLoginMember() {
        final String id = getLoginMemberId();
        try {
            return memberRepository.findMemberById(Long.parseLong(id));
        } catch (GeneralException e) {
            throw new InvalidJwtException(INVALID_TOKEN);
        }
    }

    public String getLoginMemberId() {
        String accessToken = JwtHeaderUtil.getAccessToken();
        return getSubject(accessToken);
    }
}
