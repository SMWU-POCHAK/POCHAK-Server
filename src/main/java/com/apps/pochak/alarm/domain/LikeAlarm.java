package com.apps.pochak.alarm.domain;

import com.apps.pochak.like.domain.LikeEntity;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeAlarm extends Alarm {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "like_id")
    private LikeEntity like;

    private Long likeMemberId;
    private String likeMemberHandle;
    private String likeMemberName;
    private String likeMemberProfileImage;

    private Long likedPostId;
    private String likedPostImage;

    public LikeAlarm(
            final LikeEntity like,
            final Member receiver,
            final AlarmType alarmType
    ) {
        super(receiver, alarmType);
        this.like = like;

        Member likeMember = like.getLikeMember();
        this.likeMemberId = likeMember.getId();
        this.likeMemberHandle = likeMember.getHandle();
        this.likeMemberName = likeMember.getName();
        this.likeMemberProfileImage = likeMember.getProfileImage();

        Post likedPost = like.getLikedPost();
        this.likedPostId = likedPost.getId();
        this.likedPostImage = likedPost.getPostImage();
    }
}
