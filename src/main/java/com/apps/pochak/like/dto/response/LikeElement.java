package com.apps.pochak.like.dto.response;

import com.apps.pochak.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeElement {
    private Long memberId;
    private String handle;
    private String profileImage;
    private String name;
    private Boolean follow;

    public LikeElement(
            final Member member,
            final Boolean follow
    ) {
        this.memberId = member.getId();
        this.handle = member.getHandle();
        this.profileImage = member.getProfileImage();
        this.name = member.getName();
        this.follow = follow;
    }
}
