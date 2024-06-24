package com.apps.pochak.alarm.domain;

import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.global.BaseEntity;
import com.apps.pochak.like.domain.LikeEntity;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.tag.domain.Tag;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLRestriction;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@DynamicInsert
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = PROTECTED)
public class Alarm extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "receiver_id")
    private Member receiver;

    @Setter
    @Column(columnDefinition = "boolean default false")
    private Boolean isChecked;

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "follow_id")
    private Follow follow;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "like_id")
    private LikeEntity like;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "tag_approval_id")
    private Tag tag;

    @Builder
    private Alarm(Member receiver, Boolean isChecked, AlarmType alarmType, Comment comment, Follow follow, LikeEntity like, Tag tag) {
        this.receiver = receiver;
        this.isChecked = isChecked;
        this.alarmType = alarmType;
        this.comment = comment;
        this.follow = follow;
        this.like = like;
        this.tag = tag;
    }

    public static Alarm getPostOwnerCommentAlarm(Comment comment, Member receiver) {
        return Alarm.builder()
                .comment(comment)
                .receiver(receiver)
                .alarmType(AlarmType.OWNER_COMMENT)
                .build();
    }

    public static Alarm getTaggedPostCommentAlarm(Comment comment, Member receiver) {
        return Alarm.builder()
                .comment(comment)
                .receiver(receiver)
                .alarmType(AlarmType.TAGGED_COMMENT)
                .build();
    }

    public static Alarm getCommentReplyAlarm(Comment comment, Member receiver) {
        return Alarm.builder()
                .comment(comment)
                .receiver(receiver)
                .alarmType(AlarmType.COMMENT_REPLY)
                .build();
    }

    public static Alarm getFollowAlarm(Follow follow, Member receiver) {
        return Alarm.builder()
                .follow(follow)
                .receiver(receiver)
                .alarmType(AlarmType.FOLLOW)
                .build();
    }

    public static Alarm getLikeAlarm(LikeEntity like, Member receiver) {
        return Alarm.builder()
                .like(like)
                .receiver(receiver)
                .alarmType(AlarmType.LIKE)
                .build();
    }

    public static Alarm getTagApprovalAlarm(Tag tag, Member receiver) {
        return Alarm.builder()
                .tag(tag)
                .receiver(receiver)
                .alarmType(AlarmType.TAG_APPROVAL)
                .build();
    }

    public boolean isCommentAlarm() {
        final AlarmType type = this.alarmType;
        return type.equals(AlarmType.COMMENT_REPLY) ||
                type.equals(AlarmType.TAGGED_COMMENT) ||
                type.equals(AlarmType.OWNER_COMMENT);
    }

    public boolean isFollowAlarm() {
        return this.alarmType.equals(AlarmType.FOLLOW);
    }

    public boolean isLikeAlarm() {
        return this.alarmType.equals(AlarmType.LIKE);
    }

    public boolean isTagApprovalAlarm() {
        return this.alarmType.equals(AlarmType.TAG_APPROVAL);
    }
}
