package com.apps.pochak.memories.dto.response;

import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.memories.dto.MemoriesElement;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.tag.domain.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoriesPreviewResponse {
    private String handle;
    private String loginMemberProfileImage;
    private String memberProfileImage;
    private LocalDateTime followDate;
    private LocalDateTime followedDate;
    private int followDay;
    private int pochakCount;
    private int bondedCount;
    private int pochakedCount;
    private MemoriesElement firstPochaked;
    private MemoriesElement firstPochak;
    private MemoriesElement firstPochackedWith;
    private MemoriesElement latestPost;

    @Builder(builderMethodName = "of")
    public MemoriesPreviewResponse(
            final Member loginMember,
            final Member member,
            final Follow follow,
            final Follow followed,
            final Long countTag,
            final Long countTaggedWith,
            final Long countTagged,
            final Tag firstTagged,
            final Tag firstTag,
            final Tag firstTaggedWith,
            final Tag latestTag) {
        this.handle = member.getHandle();
        this.loginMemberProfileImage = loginMember.getProfileImage();
        this.memberProfileImage = member.getProfileImage();
        this.followDate = follow.getLastModifiedDate();
        this.followedDate = followed.getLastModifiedDate();
        this.followDay = (followDate.isAfter(followedDate) ? Period.between(LocalDate.now(), followDate.toLocalDate()).getDays() : Period.between(LocalDate.now(), followedDate.toLocalDate()).getDays());
        this.pochakCount = countTag.intValue();
        this.bondedCount = countTaggedWith.intValue();
        this.pochakedCount = countTagged.intValue();
        this.firstPochaked = MemoriesElement.from(firstTagged);
        this.firstPochak = MemoriesElement.from(firstTag);
        this.firstPochackedWith = MemoriesElement.from(firstTaggedWith);
        this.latestPost = MemoriesElement.from(latestTag);
    }
}
