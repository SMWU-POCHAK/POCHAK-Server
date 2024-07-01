package com.apps.pochak.alarm.domain;

import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.member.domain.Member;
import jakarta.persistence.DiscriminatorValue;
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
@DiscriminatorValue("FOLLOW_ALARM")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowAlarm extends Alarm {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "follow_id")
    private Follow follow;

    private Long senderId;
    private String senderHandle;
    private String senderName;
    private String senderProfileImage;

    public FollowAlarm(
            final Follow follow,
            final Member receiver
    ) {
        super(receiver, FOLLOW);
        this.follow = follow;

        Member sender = follow.getSender();
        this.senderId = sender.getId();
        this.senderHandle = sender.getHandle();
        this.senderName = sender.getName();
        this.senderProfileImage = sender.getProfileImage();
    }
}
