package com.apps.pochak.login.dto.response;

import com.apps.pochak.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthMemberResponse {
    private Long id;
    private String socialId;
    private String name;
    private String email;
    private String socialType;
    private String accessToken;
    private String refreshToken;
    private Boolean isNewMember;


    @Builder
    public OAuthMemberResponse(
            final String socialId,
            final String name,
            final String email,
            final String socialType,
            final String accessToken,
            final String refreshToken,
            final Boolean isNewMember
    ) {
        this.socialId = socialId;
        this.name = name;
        this.email = email;
        this.socialType = socialType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.isNewMember = isNewMember;
    }

    public OAuthMemberResponse(
            final Member member,
            final Boolean isNewMember,
            final String accessToken
    ) {
        this.id = member.getId();
        this.socialId = member.getSocialId();
        this.email = member.getEmail();
        this.socialType = member.getSocialType().name();
        this.accessToken = accessToken;
        this.refreshToken = member.getRefreshToken();
        this.isNewMember = isNewMember;
    }
}
