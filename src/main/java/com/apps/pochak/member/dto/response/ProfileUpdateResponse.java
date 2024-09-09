package com.apps.pochak.member.dto.response;

import com.apps.pochak.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProfileUpdateResponse {
    private String name;
    private String handle;
    private String message;
    private String profileImage;

    @Builder
    public ProfileUpdateResponse(
            final Member member
    ) {
        this.name = member.getName();
        this.handle = member.getHandle();
        this.message = member.getMessage();
        this.profileImage = member.getProfileImage();
    }
}
