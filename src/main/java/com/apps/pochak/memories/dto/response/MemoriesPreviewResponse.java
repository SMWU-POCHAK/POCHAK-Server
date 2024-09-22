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
            final Tag firstPochaked,
            final Tag firstPochak,
            final Tag firstPochackedWith,
            final Tag latestPost) {
        this.handle = member.getHandle();
        this.loginMemberProfileImage = loginMember.getProfileImage();
        this.memberProfileImage = member.getProfileImage();
        this.followDate = follow.getLastModifiedDate();
        this.followedDate = followed.getLastModifiedDate();
        this.followDay = (followDate.isAfter(followedDate) ? Period.between(LocalDate.now(), followDate.toLocalDate()).getDays() : Period.between(LocalDate.now(), followedDate.toLocalDate()).getDays());
        this.firstPochaked = MemoriesElement.from(firstPochaked);
        this.firstPochak = MemoriesElement.from(firstPochak);
        this.firstPochackedWith = MemoriesElement.from(firstPochackedWith);
        this.latestPost = MemoriesElement.from(latestPost);
    }
}
