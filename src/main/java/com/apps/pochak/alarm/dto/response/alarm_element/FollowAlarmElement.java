package com.apps.pochak.alarm.dto.response.alarm_element;

import com.apps.pochak.alarm.domain.FollowAlarm;
import com.apps.pochak.alarm.dto.response.AlarmElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowAlarmElement extends AlarmElement {
    private Long memberId;
    private String memberHandle;
    private String memberName;
    private String memberProfileImage;

    public FollowAlarmElement(FollowAlarm alarm) {
        super(alarm);
        this.memberId = alarm.getSenderId();
        this.memberHandle = alarm.getSenderHandle();
        this.memberName = alarm.getSenderName();
        this.memberProfileImage = alarm.getSenderProfileImage();
    }
}
