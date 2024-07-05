package com.apps.pochak.tag.dto.response;

import com.apps.pochak.member.domain.Member;
import com.apps.pochak.tag.domain.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagElement {
    private Long memberId;
    private String profileImage;
    private String handle;
    private String name;

    public TagElement(Tag tag) {
        Member member = tag.getMember();
        this.memberId = member.getId();
        this.profileImage = member.getProfileImage();
        this.handle = member.getHandle();
        this.name = member.getName();
    }
}
