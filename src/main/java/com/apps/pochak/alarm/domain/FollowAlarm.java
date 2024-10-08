package com.apps.pochak.alarm.domain;

import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.member.domain.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.apps.pochak.alarm.domain.AlarmType.FOLLOW;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
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
}
