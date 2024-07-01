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
        this.memberId = alarm.getLikeMemberId();
        this.memberHandle = alarm.getLikeMemberHandle();
        this.memberName = alarm.getLikeMemberName();
        this.memberProfileImage = alarm.getLikeMemberProfileImage();
        this.postId = alarm.getLikedPostId();
        this.postImage = alarm.getLikedPostImage();
    }
}
