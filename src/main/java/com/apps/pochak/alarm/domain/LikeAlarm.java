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

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeAlarm extends Alarm {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "like_id")
    private LikeEntity like;

    private Long likedPostId;
    private String likedPostImage;

    public LikeAlarm(
            final Long id,
            final LikeEntity like,
            final Member receiver,
            final AlarmType alarmType
    ) {
        super(id, receiver, alarmType, like.getLikeMember());
        initializeFields(like);
    }

    public LikeAlarm(
            final LikeEntity like,
            final Member receiver,
            final AlarmType alarmType
    ) {
        super(receiver, alarmType, like.getLikeMember());
        initializeFields(like);
    }

    private void initializeFields(LikeEntity like) {
        this.like = like;

        Post likedPost = like.getLikedPost();
        this.likedPostId = likedPost.getId();
        this.likedPostImage = likedPost.getPostImage();
    }
}
