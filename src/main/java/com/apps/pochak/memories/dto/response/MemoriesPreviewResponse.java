package com.apps.pochak.memories.dto.response;

import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.memories.dto.MemoriesElement;
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
    private Long pochakCount;
    private Long bondedCount;
    private Long pochakedCount;
    private MemoriesElement firstPochaked;
    private MemoriesElement firstPochak;
    private MemoriesElement firstBonded;
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
            final Tag latestTag
    ) {
        this.handle = member.getHandle();
        this.loginMemberProfileImage = loginMember.getProfileImage();
        this.memberProfileImage = member.getProfileImage();
        this.followDate = follow.getLastModifiedDate();
        this.followedDate = followed.getLastModifiedDate();
        this.followDay = findFollowDay(followDate, followedDate);
        this.pochakCount = countTag;
        this.bondedCount = countTaggedWith;
        this.pochakedCount = countTagged;
        this.firstPochaked = MemoriesElement.from(firstTagged);
        this.firstPochak = MemoriesElement.from(firstTag);
        this.firstBonded = MemoriesElement.from(firstTaggedWith);
        this.latestPost = MemoriesElement.from(latestTag);
    }

    private int findFollowDay(LocalDateTime followDate, LocalDateTime followedDate) {
        if (followDate.isAfter(followedDate)) {
            return Period.between(followDate.toLocalDate(), LocalDate.now()).getDays();
        } else
            return Period.between(followedDate.toLocalDate(), LocalDate.now()).getDays();
    }
}
