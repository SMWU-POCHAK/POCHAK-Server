package com.apps.pochak.alarm.domain;

import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.member.domain.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import static com.apps.pochak.alarm.domain.AlarmType.FOLLOW;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowAlarm extends Alarm {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "follow_id")
    private Follow follow;

    public FollowAlarm(
            final Long id,
            final Follow follow,
            final Member receiver
    ) {
        super(id, receiver, FOLLOW, follow.getSender());
        initializeFields(follow);
    }

    public FollowAlarm(
            final Follow follow,
            final Member receiver
    ) {
        super(receiver, FOLLOW, follow.getSender());
        initializeFields(follow);
    }

    private void initializeFields(final Follow follow) {
        this.follow = follow;
    }

    @Override
    public String getPushNotificationTitle() {
        return this.getAlarmType().getTitle();
    }

    @Override
    public String getPushNotificationBody() {
        return String.format(this.getAlarmType().getBody(), this.getSenderName());
    }

    @Override
    public String getPushNotificationImage() {
        return String.format(this.getAlarmType().getImage(), this.getSenderProfileImage());
    }
}
