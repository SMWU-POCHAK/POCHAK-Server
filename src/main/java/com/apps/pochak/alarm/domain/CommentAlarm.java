package com.apps.pochak.alarm.domain;

import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.post.domain.Post;
import jakarta.persistence.DiscriminatorValue;
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
@DiscriminatorValue("COMMENT_ALARM")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentAlarm extends Alarm {

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;
    private String content;

    private Long postId;
    private String postImage;

    private Long writerId;
    private String writerHandle;
    private String writerName;
    private String writerProfileImage;

    public CommentAlarm(
            final Comment comment,
            final Member receiver,
            final AlarmType alarmType
    ) {
        super(receiver, alarmType);
        this.comment = comment;
        this.content = comment.getContent();

        Post post = comment.getPost();
        this.postId = post.getId();
        this.postImage = post.getPostImage();

        Member writer = comment.getMember();
        this.writerId = writer.getId();
        this.writerHandle = writer.getHandle();
        this.writerName = writer.getName();
        this.writerProfileImage = writer.getProfileImage();
    }
}
