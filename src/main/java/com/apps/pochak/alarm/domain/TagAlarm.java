package com.apps.pochak.alarm.domain;

import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.tag.domain.Tag;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.apps.pochak.alarm.domain.AlarmType.TAG_APPROVAL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagAlarm extends Alarm {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "tag_approval_id")
    private Tag tag;

    private Long postId;
    private String postImage;

    public TagAlarm(
            final Long id,
            final Tag tag,
            final Member tagger,
            final Member receiver
    ) {
        super(id, receiver, TAG_APPROVAL, tagger);
        initializeFields(tag);
    }

    public TagAlarm(
            final Tag tag,
            final Member tagger,
            final Member receiver
    ) {
        super(receiver, TAG_APPROVAL, tagger);
        initializeFields(tag);
    }

    private void initializeFields(
            final Tag tag
    ) {
        this.tag = tag;

        Post post = tag.getPost();
        this.postId = post.getId();
        this.postImage = post.getPostImage();
    }

    @Override
    public String getPushNotificationImage() {
        return String.format(this.getAlarmType().getImage(), this.postImage);
    }
}
