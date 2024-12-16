package com.apps.pochak.memories.dto.response;

import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.memories.domain.MemoriesType;
import com.apps.pochak.memories.dto.MemoriesElement;
import com.apps.pochak.tag.domain.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

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
    private Map<MemoriesType, MemoriesElement> memories = new HashMap<>();
    private Map<LocalDateTime, MemoriesType> timeLine = new TreeMap<>(Comparator.reverseOrder());

    @Builder(builderMethodName = "of")
    public MemoriesPreviewResponse(
            final Member loginMember,
            final Member member,
            final Follow follow,
            final Follow followed,
            final Long countTag,
            final Long countTaggedWith,
            final Long countTagged,
            final Map<MemoriesType, Tag> tags
            ) {
        this.handle = member.getHandle();
        this.loginMemberProfileImage = loginMember.getProfileImage();
        this.memberProfileImage = member.getProfileImage();
        this.followDate = follow.getLastModifiedDate();
        this.timeLine.put(this.followDate, MemoriesType.Follow);
        this.followedDate = followed.getLastModifiedDate();
        this.timeLine.put(this.followedDate, MemoriesType.Followed);
        this.followDay = findFollowDay(followDate, followedDate);
        this.pochakCount = countTag;
        this.bondedCount = countTaggedWith;
        this.pochakedCount = countTagged;
        for (MemoriesType memoriesType : tags.keySet()) {
            this.memories.put(memoriesType, MemoriesElement.from(tags.get(memoriesType)));
            if (tags.get(memoriesType) != null) {
                this.timeLine.put(tags.get(memoriesType).getPost().getAllowedDate(), memoriesType);
            }
        }
    }

    private int findFollowDay(LocalDateTime followDate, LocalDateTime followedDate) {
        if (followDate.isAfter(followedDate)) {
            return Period.between(followDate.toLocalDate(), LocalDate.now()).getDays();
        } else
            return Period.between(followedDate.toLocalDate(), LocalDate.now()).getDays();
    }
}
