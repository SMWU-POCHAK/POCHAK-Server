package com.apps.pochak.login.jwt;

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
    private final MemberRepository memberRepository;
    private final long accessTokenExpirationTime = 1000L * 60 * 60 * 24 * 30 * 30;
    private final long refreshTokenExpirationTime = 1000L * 60 * 60 * 24 * 30;
    @Value("${jwt.secretKey}")
    private String secretKey;
    private Key key;

    @PostConstruct
    private void _getSecretKey() {
        String keyBase64Encoded = Base64.getEncoder().encodeToString(secretKey.getBytes());
        key = Keys.hmacShaKeyFor(keyBase64Encoded.getBytes());
    }

    public String createAccessToken(String userPK) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(userPK)
                .claim(AUTHORITIES_KEY, "ROLE_USER")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenExpirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken() {
        Date now = new Date();
        return Jwts.builder()
                .setExpiration(new Date(now.getTime() + refreshTokenExpirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String validateRefreshToken(String accessToken, String refreshToken) {
        String handle = getHandle(accessToken);

        Member member = memberRepository.findByHandleWithoutLogin(handle);

        if (member.getRefreshToken() == null || member.getRefreshToken().isEmpty())
            throw new RefreshTokenException(NULL_REFRESH_TOKEN);

        if (!member.getRefreshToken().equals(refreshToken))
            throw new RefreshTokenException(INVALID_REFRESH_TOKEN);

        return member.getHandle();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return true;
        } catch (SecurityException e) {
            throw new JwtException(INVALID_TOKEN_SIGNATURE.getMessage());
        } catch (MalformedJwtException e) {
            throw new JwtException(MALFORMED_TOKEN.getMessage());
        } catch (ExpiredJwtException e) {
            throw new JwtException(EXPIRED_TOKEN.getMessage());
        } catch (UnsupportedJwtException e) {
            throw new JwtException(UNSUPPORTED_TOKEN.getMessage());
        } catch (IllegalArgumentException e) {
            throw new JwtException(INVALID_TOKEN.getMessage());
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

    public String getHandle(String token) {
        return getTokenClaims(token).getSubject();
    }

    public PostTokenResponse reissueAccessToken() {
        String accessToken = JwtHeaderUtil.getAccessToken();
        String refreshToken = JwtHeaderUtil.getRefreshToken();

        if (refreshToken == null)
            throw new RefreshTokenException(NULL_REFRESH_TOKEN);
        else if (!validate(refreshToken))
            throw new RefreshTokenException(INVALID_REFRESH_TOKEN);

        String handle = validateRefreshToken(accessToken, refreshToken);
        String newAccessToken = createAccessToken(handle);

        return PostTokenResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    // custom
    public Member getLoginMember() {
        final String loginMemberHandle = getLoginMemberHandle();
        return memberRepository.findByHandleWithoutLogin(loginMemberHandle);
    }

    public String getLoginMemberHandle() {
        String accessToken = JwtHeaderUtil.getAccessToken();
        return getHandle(accessToken);
    }
}
