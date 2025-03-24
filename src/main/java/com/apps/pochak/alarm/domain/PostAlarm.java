package com.apps.pochak.alarm.domain;

import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostAlarm extends Alarm {
    private Long postId;
    private String postImage;

    private Long memberId;
    private String memberHandle;
    private String memberName;
    private String memberProfileImage;

    public PostAlarm(
            final Long id,
            final Post post,
            final Member receiver,
            final AlarmType alarmType
    ) {
        super(id, receiver, alarmType, post.getOwner());
        initializeFields(post);
    }

    public PostAlarm(
            final Post post,
            final Member receiver,
            final AlarmType alarmType
    ) {
        super(receiver, alarmType, post.getOwner());
        initializeFields(post);
    }

    private void initializeFields(final Post post) {
        this.postId = post.getId();
        this.postImage = post.getPostImage();

        Member pinnedMember = post.getPinnedMember();
        this.memberId = pinnedMember.getId();
        this.memberHandle = pinnedMember.getHandle();
        this.memberName = pinnedMember.getName();
        this.memberProfileImage = pinnedMember.getProfileImage();
    }

    @Override
    public String getPushNotificationBody() {
        return String.format(this.getAlarmType().getBody(), getSender().getName(), memberName);
    }

    @Override
    public String getPushNotificationImage() {
        return String.format(this.getAlarmType().getImage(), this.postImage);
    }
}
