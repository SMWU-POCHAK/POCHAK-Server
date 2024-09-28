package com.apps.pochak.alarm.dto.response.alarm_element;

import com.apps.pochak.alarm.domain.LikeAlarm;
import com.apps.pochak.alarm.dto.response.AlarmElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeAlarmElement extends AlarmElement {
    private Long senderId;
    private String senderHandle;
    private String senderName;
    private String senderProfileImage;

    private Long postId;
    private String postImage;

    public LikeAlarmElement(LikeAlarm alarm) {
        super(alarm);
        this.senderId = alarm.getSenderId();
        this.senderHandle = alarm.getSenderHandle();
        this.senderName = alarm.getSenderName();
        this.senderProfileImage = alarm.getSenderProfileImage();
        this.postId = alarm.getLikedPostId();
        this.postImage = alarm.getLikedPostImage();
    }
}
