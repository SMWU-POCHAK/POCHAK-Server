package com.apps.pochak.alarm.dto.response.alarm_element;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.TagAlarm;
import com.apps.pochak.alarm.dto.response.AlarmElement;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.tag.domain.Tag;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagApprovalAlarmElement extends AlarmElement {
    private Long tagId;

    private Long senderId;
    private String senderHandle;
    private String senderName;
    private String senderProfileImage;

    private Long postId;
    private String postImage;

    public TagApprovalAlarmElement(final TagAlarm alarm) {
        super(alarm);
        this.tagId = alarm.getTag().getId();
        this.senderId = alarm.getSenderId();
        this.senderHandle = alarm.getSenderHandle();
        this.senderName = alarm.getSenderName();
        this.senderProfileImage = alarm.getSenderProfileImage();
        this.postId = alarm.getPostId();
        this.postImage = alarm.getPostImage();
    }
}
