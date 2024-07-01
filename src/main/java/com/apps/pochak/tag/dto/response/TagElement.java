package com.apps.pochak.tag.dto.response;

import com.apps.pochak.member.domain.Member;
import com.apps.pochak.tag.domain.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TagElement {
    private Long memberId;
    private String handle;
    private String name;

    @Builder
    public TagElement(Long memberId, String handle, String name) {
        this.memberId = memberId;
        this.handle = handle;
        this.name = name;
    }

    public TagElement(Tag tag) {
        Member member = tag.getMember();
        this.memberId = member.getId();
        this.handle = member.getHandle();
        this.name = member.getName();
    }
}
