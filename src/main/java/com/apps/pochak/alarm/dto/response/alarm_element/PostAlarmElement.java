package com.apps.pochak.alarm.dto.response.alarm_element;

import com.apps.pochak.alarm.domain.PostAlarm;
import com.apps.pochak.alarm.dto.response.AlarmElement;
import com.apps.pochak.member.domain.Member;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostAlarmElement extends AlarmElement {
    private Long ownerId;
    private String ownerHandle;
    private String ownerName;
    private String ownerProfileImage;

    private Long memberId;
    private String memberHandle;
    private String memberName;
    private String memberProfileImage;

    private Long postId;
    private String postImage;

    public PostAlarmElement(final PostAlarm alarm) {
        super(alarm);
        Member sender = alarm.getSender();
        this.ownerId = sender.getId();
        this.ownerHandle = sender.getHandle();
        this.ownerName = sender.getName();
        this.ownerProfileImage = sender.getProfileImage();

        this.memberId = alarm.getMemberId();
        this.memberHandle = alarm.getMemberHandle();
        this.memberName = alarm.getMemberName();
        this.memberProfileImage = alarm.getMemberProfileImage();

        this.postId = alarm.getPostId();
        this.postImage = alarm.getPostImage();
    }
}
