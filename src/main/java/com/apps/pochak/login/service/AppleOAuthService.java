package com.apps.pochak.login.service;

import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.global.api_payload.exception.handler.AppleOAuthException;
import com.apps.pochak.login.client.AppleClient;
import com.apps.pochak.login.dto.apple.ApplePublicKeyResponse;
import com.apps.pochak.login.dto.apple.AppleTokenResponse;
import com.apps.pochak.login.dto.response.OAuthMemberResponse;
import com.apps.pochak.login.provider.JwtProvider;
import com.apps.pochak.login.util.ApplePublicKeyGenerator;
import com.apps.pochak.login.util.JwtValidator;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.SocialType;
import com.apps.pochak.member.domain.repository.MemberRepository;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleOAuthService {
    private final JwtProvider jwtProvider;
    private final JwtValidator jwtValidator;
    private final MemberRepository memberRepository;
    private final ApplePublicKeyGenerator applePublicKeyGenerator;
    private final AppleClient appleClient;

    @Value("${oauth2.apple.key-id}")
    private String KEY_ID;
    @Value("${oauth2.apple.team-id}")
    private String TEAM_ID;
    @Value("${oauth2.apple.client-id}")
    private String CLIENT_ID;
    @Value("${oauth2.apple.key-id-path}")
    private String KEY_ID_PATH;

    @Transactional
    public OAuthMemberResponse login(final String idToken, final String authorizationCode) {
        Map<String, String> tokenHeaders = jwtValidator.parseHeaders(idToken);
        Claims claims = verifyIdToken(tokenHeaders, idToken);

        String sub = String.valueOf(claims.get("sub"));
        String email = String.valueOf(claims.get("email"));

        Member member = memberRepository.findMemberBySocialId(sub).orElse(null);

        if (member == null) {
            String appleRefreshToken = getAppleRefreshToken(authorizationCode);
            return OAuthMemberResponse.builder()
                    .socialId(sub)
                    .email(email)
                    .socialType(SocialType.APPLE.name())
                    .refreshToken(appleRefreshToken)
                    .isNewMember(true)
                    .build();
        }

        String appRefreshToken = jwtProvider.createRefreshToken();
        String appAccessToken = jwtProvider.createAccessToken(member.getId().toString());

        member.updateRefreshToken(appRefreshToken);

        return OAuthMemberResponse.builder()
                .socialId(sub)
                .email(email)
                .handle(member.getHandle())
                .socialType("apple")
                .accessToken(appAccessToken)
                .refreshToken(appRefreshToken)
                .isNewMember(false)
                .build();
    }

    /**
     * Get Public Key
     */
    private Claims verifyIdToken(final Map<String, String> tokenHeaders, final String idToken) {
        try {
            ApplePublicKeyResponse publicKeyResponse = appleClient
                    .getPublicKey()
                    .orElseThrow(() -> new AppleOAuthException(INVALID_PUBLIC_KEY));

            PublicKey publicKey = applePublicKeyGenerator.generatePublicKey(tokenHeaders, publicKeyResponse);
            return jwtValidator.getTokenClaims(idToken, publicKey);
        } catch (MalformedJwtException e) {
            throw new AppleOAuthException(MALFORMED_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new AppleOAuthException(EXPIRED_ACCESS_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new AppleOAuthException(UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new AppleOAuthException(INVALID_ACCESS_TOKEN);
        }
    }

    /**
     * Get Apple Refresh Token
     * For Delete Account
     */
    private String getAppleRefreshToken(final String authorizationCode) {
        AppleTokenResponse appleToken = appleClient.getRefreshToken(
                makeClientSecret(),
                authorizationCode,
                "authorization_code",
                CLIENT_ID
        ).orElseThrow(() -> new AppleOAuthException(FAIL_GET_REFRESH_TOKEN));

        return appleToken.getRefreshToken();
    }

    private String makeClientSecret() {
        Date expirationDate = Date
                .from(LocalDateTime
                        .now()
                        .plusDays(30)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                );

        Map<String, Object> jwtHeader = new HashMap<>();
        jwtHeader.put("kid", KEY_ID);
        jwtHeader.put("alg", "ES256");

        return Jwts.builder()
                .setHeaderParams(jwtHeader)
                .setIssuer(TEAM_ID)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .setAudience("https://appleid.apple.com")
                .setSubject(CLIENT_ID)
                .signWith(SignatureAlgorithm.ES256, getPrivateKey())
                .compact();
    }

    private PrivateKey getPrivateKey() {
        try {
            ClassPathResource resource = new ClassPathResource(KEY_ID_PATH);
            String privateKey = new String(Files.readAllBytes(Paths.get(resource.getURI())));

            Reader pemReader = new StringReader(privateKey);
            PEMParser pemParser = new PEMParser(pemReader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
            return converter.getPrivateKey(object);
        } catch (IOException e) {
            throw new GeneralException(IO_EXCEPTION);
        }
    }

    /**
     * Revoke Apple Login
     */
    public void revoke(final String socialRefreshToken) {
        appleClient.revoke(makeClientSecret(), socialRefreshToken, CLIENT_ID);
    }
}
