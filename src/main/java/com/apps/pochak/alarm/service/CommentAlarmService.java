package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.AlarmType;
import com.apps.pochak.alarm.domain.CommentAlarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.fcm.service.FCMService;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentAlarmService {
    private final AlarmRepository alarmRepository;
    private final TagRepository tagRepository;
    private final FCMService fcmService;

    public void sendParentCommentAlarm(
            final Comment savedComment,
            final Member postOwner
    ) {
        sendPostOwnerCommentAlarm(savedComment, postOwner);
        sendTaggedPostCommentAlarm(savedComment, null);
    }

    public void sendChildCommentAlarm(
            final Comment savedComment,
            final Member postOwner,
            final Member parentCommentWriter
    ) {
        sendCommentReplyAlarm(savedComment, parentCommentWriter);
        if (!postOwner.equals(parentCommentWriter)) sendPostOwnerCommentAlarm(savedComment, postOwner);
        sendTaggedPostCommentAlarm(savedComment, parentCommentWriter);
    }

    private void sendCommentReplyAlarm(
            final Comment childComment,
            final Member parentCommentWriter
    ) {
        if (isSenderEqualToReceiver(childComment, parentCommentWriter)) return;

        final CommentAlarm alarm = new CommentAlarm(
                childComment,
                parentCommentWriter,
                AlarmType.COMMENT_REPLY
        );

        alarmRepository.save(alarm);
        fcmService.sendPushNotification(alarm);
    }

    private void sendPostOwnerCommentAlarm(
            final Comment comment,
            final Member owner
    ) {
        if (isSenderEqualToReceiver(comment, owner)) return;

        final CommentAlarm alarm = new CommentAlarm(
                comment,
                owner,
                AlarmType.OWNER_COMMENT
        );
        alarmRepository.save(alarm);
        fcmService.sendPushNotification(alarm);
    }

    private void sendTaggedPostCommentAlarm(
            final Comment comment,
            final Member excludeMember
    ) {
        final List<Tag> tagList = tagRepository.findTagsByPost(comment.getPost());
        final List<Alarm> alarmList = new ArrayList<>();
        for (Tag tag : tagList) {
            final Member taggedMember = tag.getMember();
            if (isSenderEqualToReceiver(comment, taggedMember)) continue;
            if (taggedMember.equals(excludeMember)) continue;
            alarmList.add(
                    new CommentAlarm(
                            comment,
                            taggedMember,
                            AlarmType.TAGGED_COMMENT
                    )
            );
        }
        alarmRepository.saveAll(alarmList);
        fcmService.sendPushNotification(alarmList);
    }

    private boolean isSenderEqualToReceiver(
            final Comment comment,
            final Member receiver
    ) {
        return comment.getMember().equals(receiver);
    }

    public void deleteAlarmByComment(Comment comment) {
        alarmRepository.deleteAlarmByComment(comment.getId());
    }
}
