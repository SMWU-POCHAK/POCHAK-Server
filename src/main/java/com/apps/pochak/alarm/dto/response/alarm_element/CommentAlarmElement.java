package com.apps.pochak.alarm.dto.response.alarm_element;

import com.apps.pochak.alarm.domain.CommentAlarm;
import com.apps.pochak.alarm.dto.response.AlarmElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentAlarmElement extends AlarmElement {
    private Long commentId;
    private String commentContent;

    private Long postId;
    private String postImage;

    private Long memberId;
    private String memberHandle;
    private String memberName;
    private String memberProfileImage;

    public CommentAlarmElement(CommentAlarm alarm) {
        super(alarm);
        this.commentId = alarm.getComment().getId();
        this.commentContent = alarm.getContent();
        this.postId = alarm.getPostId();
        this.postImage = alarm.getPostImage();
        this.memberId = alarm.getSender().getId();
        this.memberHandle = alarm.getSender().getHandle();
        this.memberName = alarm.getSender().getName();
        this.memberProfileImage = alarm.getSender().getProfileImage();
    }
}
