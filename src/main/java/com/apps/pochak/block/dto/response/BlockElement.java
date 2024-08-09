package com.apps.pochak.block.dto.response;

import com.apps.pochak.block.domain.Block;
import com.apps.pochak.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockElement {
    private Long memberId;
    private String profileImage;
    private String handle;
    private String name;

    @Builder(builderMethodName = "from")
    public BlockElement(final Block block) {
        Member blockedMember = block.getBlockedMember();
        this.memberId = blockedMember.getId();
        this.profileImage = blockedMember.getProfileImage();
        this.handle = blockedMember.getHandle();
        this.name = blockedMember.getName();
    }
}
