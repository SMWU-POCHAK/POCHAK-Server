package com.apps.pochak.login.service;

import com.apps.pochak.global.api_payload.exception.handler.GoogleOAuthException;
import com.apps.pochak.login.client.GoogleClient;
import com.apps.pochak.login.dto.google.GoogleMemberResponse;
import com.apps.pochak.login.dto.google.GoogleTokenResponse;
import com.apps.pochak.login.dto.response.OAuthMemberResponse;
import com.apps.pochak.login.provider.JwtProvider;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.INVALID_OAUTH_TOKEN;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.INVALID_USER_INFO;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {
    private final MemberRepository memberRepository;
    private final GoogleClient googleClient;
    private final JwtProvider jwtProvider;

    @Value("${oauth2.google.client-id}")
    private String GOOGLE_CLIENT_ID;
    @Value("${oauth2.google.redirect-uri}")
    private String GOOGLE_REDIRECT_URI;
    @Value("${oauth2.google.client-secret}")
    private String GOOGLE_CLIENT_SECRET;

    @Transactional
    public OAuthMemberResponse login(final String accessToken) {
        GoogleMemberResponse memberResponse = getUserInfo(accessToken);

        Member member = memberRepository.findMemberBySocialId(memberResponse.getId()).orElse(null);

        if (member == null) {
            return OAuthMemberResponse.builder()
                    .socialId(memberResponse.getId())
                    .name(memberResponse.getName())
                    .email(memberResponse.getEmail())
                    .socialType("google")
                    .isNewMember(true)
                    .build();
        }

        String appRefreshToken = jwtProvider.createRefreshToken();
        String appAccessToken = jwtProvider.createAccessToken(member.getId().toString());

        member.updateRefreshToken(appRefreshToken);
        memberRepository.save(member);
        return OAuthMemberResponse.builder()
                .socialId(memberResponse.getId())
                .name(memberResponse.getName())
                .email(memberResponse.getEmail())
                .handle(member.getHandle())
                .socialType("google")
                .accessToken(appAccessToken)
                .refreshToken(appRefreshToken)
                .isNewMember(false)
                .build();
    }

    public GoogleMemberResponse getUserInfo(final String accessToken) {
        return googleClient
                .getPublicKey(accessToken)
                .orElseThrow(() -> new GoogleOAuthException(INVALID_USER_INFO));
    }

    public String getAccessToken(final String code) {
        GoogleTokenResponse googleTokenResponse = googleClient.getGoogleAccessToken(
                "authorization_code",
                GOOGLE_CLIENT_ID,
                GOOGLE_CLIENT_SECRET,
                GOOGLE_REDIRECT_URI,
                code
        ).orElseThrow(() -> new GoogleOAuthException(INVALID_OAUTH_TOKEN));

        return googleTokenResponse.getAccessToken();
    }
}
