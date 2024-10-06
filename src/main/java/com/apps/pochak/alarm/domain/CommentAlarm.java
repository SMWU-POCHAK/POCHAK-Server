package com.apps.pochak.alarm.domain;

import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentAlarm extends Alarm {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;
    private String content;
    private Long parentCommentId;

    private Long postId;
    private String postImage;

    public CommentAlarm(
            final Long id,
            final Comment comment,
            final Member receiver,
            final AlarmType alarmType
    ) {
        super(id, receiver, alarmType, comment.getMember());
        initializeFields(comment);
    }

    public CommentAlarm(
            final Comment comment,
            final Member receiver,
            final AlarmType alarmType
    ) {
        super(receiver, alarmType, comment.getMember());
        initializeFields(comment);
    }

    private void initializeFields(final Comment comment) {
        this.comment = comment;
        this.content = comment.getContent();

        if (comment.isChildComment()) {
            this.parentCommentId = comment.getParentComment().getId();
        }

        Post post = comment.getPost();
        this.postId = post.getId();
        this.postImage = post.getPostImage();

    }

    @Override
    public String getPushNotificationTitle() {
        return String.format(this.getAlarmType().getTitle(), this.getSender().getName());
    }

    @Override
    public String getPushNotificationBody() {
        return String.format(this.getAlarmType().getBody(), this.content);
    }
}
