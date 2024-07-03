package com.apps.pochak.login.service;

import com.apps.pochak.global.api_payload.exception.handler.AppleOAuthException;
import com.apps.pochak.login.dto.apple.AppleTokenResponse;
import com.apps.pochak.login.dto.apple.key.ApplePublicKeyResponse;
import com.apps.pochak.login.dto.response.OAuthMemberResponse;
import com.apps.pochak.login.provider.JwtProvider;
import com.apps.pochak.login.util.ApplePublicKeyGenerator;
import com.apps.pochak.login.util.JwtValidator;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.SocialType;
import com.apps.pochak.member.domain.repository.MemberRepository;
import io.jsonwebtoken.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
import java.util.Map;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleOAuthService {
    private final JwtProvider jwtProvider;
    private final ApplePublicKeyGenerator applePublicKeyGenerator;
    private final JwtValidator jwtValidator;
    private final MemberRepository memberRepository;

    @Value("${oauth2.apple.key-id}")
    private String KEY_ID;
    @Value("${oauth2.apple.team-id}")
    private String TEAM_ID;
    @Value("${oauth2.apple.client-id}")
    private String CLIENT_ID;
    @Value("${oauth2.apple.base-url}")
    private String PUBLIC_KEY_URL;
    @Value("${oauth2.apple.key-id-path}")
    private String KEY_ID_PATH;

    @Transactional
    public OAuthMemberResponse login(String idToken, String authorizationCode) {
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
                    .isNewMember(false)
                    .build();
        }

        String appRefreshToken = jwtProvider.createRefreshToken();
        String appAccessToken = jwtProvider.createAccessToken(member.getId().toString());

        member.updateRefreshToken(appRefreshToken);

        return OAuthMemberResponse.builder()
                .socialId(sub)
                .email(email)
                .socialType("apple")
                .accessToken(appAccessToken)
                .refreshToken(appRefreshToken)
                .isNewMember(false)
                .build();
    }

    /**
     * Get Public Key
     */
    private Claims verifyIdToken(Map<String, String> tokenHeaders, String idToken) {
        try {
            WebClient webClient = WebClient
                    .builder()
                    .baseUrl(PUBLIC_KEY_URL)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            ApplePublicKeyResponse publicKeyResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/auth/keys")
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new RuntimeException("Client Error")))
                    .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new RuntimeException("Internal Server Error")))
                    .bodyToMono(ApplePublicKeyResponse.class)
                    .flux()
                    .toStream()
                    .findFirst()
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
    private String getAppleRefreshToken(String authorizationCode) {
        WebClient webClient = WebClient
                .builder()
                .baseUrl(PUBLIC_KEY_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        AppleTokenResponse appleTokenResponse =
                webClient
                        .post()
                        .uri(uriBuilder -> {
                            try {
                                return uriBuilder
                                        .path("/auth/token")
                                        .queryParam("client_id", CLIENT_ID)
                                        .queryParam("code", authorizationCode)
                                        .queryParam("client_secret", makeClientSecret())
                                        .queryParam("grant_type", "authorization_code")
                                        .build();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new RuntimeException("Social Access Token is unauthorized")))
                        .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new RuntimeException("Internal Server Error")))
                        .bodyToMono(AppleTokenResponse.class)
                        .flux()
                        .toStream()
                        .findFirst()
                        .orElseThrow(() -> new AppleOAuthException(INVALID_OAUTH_TOKEN));

        return appleTokenResponse.getRefreshToken();
    }

    private String makeClientSecret() throws IOException {
        Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .setHeaderParam("kid", KEY_ID)
                .setHeaderParam("alg", "ES256")
                .setIssuer(TEAM_ID)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .setAudience("https://appleid.apple.com")
                .setSubject(CLIENT_ID)
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                .compact();
    }

    private PrivateKey getPrivateKey() throws IOException {
        ClassPathResource resource = new ClassPathResource(KEY_ID_PATH);
        String privateKey = new String(Files.readAllBytes(Paths.get(resource.getURI())));
        Reader pemReader = new StringReader(privateKey);
        PEMParser pemParser = new PEMParser(pemReader);
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
        return converter.getPrivateKey(object);
    }

    /**
     * Revoke Apple Login
     */
    public String revoke(String socialRefreshToken) {
        WebClient webClient = WebClient
                .builder()
                .baseUrl(PUBLIC_KEY_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();

        return webClient
                .post()
                .uri(uriBuilder -> {
                    try {
                        return uriBuilder
                                .path("/auth/revoke")
                                .queryParam("client_id", CLIENT_ID)
                                .queryParam("client_secret", makeClientSecret())
                                .queryParam("token", socialRefreshToken)
                                .build();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
