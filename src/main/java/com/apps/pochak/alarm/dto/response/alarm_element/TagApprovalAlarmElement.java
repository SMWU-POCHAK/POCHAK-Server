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

    private Long ownerId;
    private String ownerHandle;
    private String ownerName;
    private String ownerProfileImage;

    private Long postId;
    private String postImage;

    public TagApprovalAlarmElement(final TagAlarm alarm) {
        super(alarm);
        this.tagId = alarm.getTag().getId();
        this.ownerId = alarm.getSender().getId();
        this.ownerHandle = alarm.getSender().getHandle();
        this.ownerName = alarm.getSender().getName();
        this.ownerProfileImage = alarm.getSender().getProfileImage();
        this.postId = alarm.getPostId();
        this.postImage = alarm.getPostImage();
    }
}
