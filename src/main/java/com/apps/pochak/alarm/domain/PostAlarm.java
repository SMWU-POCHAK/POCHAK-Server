package com.apps.pochak.alarm.domain;

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
public class PostAlarm extends Alarm {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id")
    private Post post;
    private String postImage;

    private Long memberId;
    private String memberHandle;
    private String memberName;
    private String memberProfile;

    public PostAlarm(
            final Post post,
            final Member receiver,
            final AlarmType alarmType
    ) {
        super(receiver, alarmType, post.getOwner());
        initializeFields(post);
    }

    private void initializeFields(final Post post) {
        this.post = post;
        this.postImage = post.getPostImage();

        Member pinnedMember = post.getPinnedMember();
        this.memberId = pinnedMember.getId();
        this.memberHandle = pinnedMember.getHandle();
        this.memberName = pinnedMember.getName();
        this.memberProfile = pinnedMember.getProfileImage();
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
