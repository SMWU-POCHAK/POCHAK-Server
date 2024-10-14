package com.apps.pochak.alarm.dto.response.alarm_element;

import com.apps.pochak.alarm.domain.LikeAlarm;
import com.apps.pochak.alarm.dto.response.AlarmElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeAlarmElement extends AlarmElement {
    private Long memberId;
    private String memberHandle;
    private String memberName;
    private String memberProfileImage;

    private Long postId;
    private String postImage;

    public LikeAlarmElement(LikeAlarm alarm) {
        super(alarm);
        this.memberId = alarm.getSender().getId();
        this.memberHandle = alarm.getSender().getHandle();
        this.memberName = alarm.getSender().getName();
        this.memberProfileImage = alarm.getSender().getProfileImage();
        this.postId = alarm.getPostId();
        this.postImage = alarm.getPostImage();
    }
}
