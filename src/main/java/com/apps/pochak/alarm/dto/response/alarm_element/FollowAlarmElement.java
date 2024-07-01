package com.apps.pochak.alarm.dto.response.alarm_element;

import com.apps.pochak.alarm.domain.FollowAlarm;
import com.apps.pochak.alarm.dto.response.AlarmElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowAlarmElement extends AlarmElement {
    private Long id;
    private String handle;
    private String name;
    private String profileImage;

    public FollowAlarmElement(FollowAlarm alarm) {
        super(alarm);
        this.id = alarm.getSenderId();
        this.handle = alarm.getSenderHandle();
        this.name = alarm.getSenderName();
        this.profileImage = alarm.getSenderProfileImage();
    }
}
