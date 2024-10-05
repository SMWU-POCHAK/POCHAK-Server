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

    @Override
    public String getPushNotificationTitle() {
        return this.getAlarmType().getTitle();
    }

    @Override
    public String getPushNotificationBody() {
        return String.format(this.getAlarmType().getBody(), this.getSender().getName());
    }

    @Override
    public String getPushNotificationImage() {
        return String.format(this.getAlarmType().getImage(), this.getSender().getProfileImage());
    }
}
