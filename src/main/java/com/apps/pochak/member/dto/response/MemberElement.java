package com.apps.pochak.member.dto.response;

import com.apps.pochak.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberElement {
    private Long memberId;
    private String profileImage;
    private String handle;
    private String name;
    private Boolean isFollow;

    public MemberElement(
            final Long id,
            final String profileImage,
            final String handle,
            final String name,
            final Long isFollow
    ) {
        this.memberId = id;
        this.profileImage = profileImage;
        this.handle = handle;
        this.name = name;
        this.isFollow = convert(isFollow);
    }

    public MemberElement(final Member member) {
        this.memberId = member.getId();
        this.profileImage = member.getProfileImage();
        this.handle = member.getHandle();
        this.name = member.getName();
    }

    private Boolean convert(Long value) {
        if (value == null) {
            return null;
        } else {
            return value != 0;
        }
    }
}
