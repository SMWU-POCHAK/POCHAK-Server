package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.AlarmType;
import com.apps.pochak.alarm.domain.LikeAlarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.like.domain.LikeEntity;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.apps.pochak.alarm.domain.AlarmType.OWNER_LIKE;

@Service
@RequiredArgsConstructor
public class LikeAlarmService {
    private final AlarmRepository alarmRepository;
    private final TagRepository tagRepository;

    public void sendLikeAlarm(
            final LikeEntity like,
            final Member postOwner
    ) {
        sendPostOwnerLikeAlarm(like, postOwner);
        sendTaggedPostLikeAlarm(like);
    }

    private void sendPostOwnerLikeAlarm(
            final LikeEntity like,
            final Member owner
    ) {
        if (isSenderEqualToReceiver(like, owner)) return;

        final Alarm likeAlarm = new LikeAlarm(
                like,
                owner,
                OWNER_LIKE
        );
        alarmRepository.save(likeAlarm);
    }

    private void sendTaggedPostLikeAlarm(
            final LikeEntity like
    ) {
        final List<Tag> tagList = tagRepository.findTagsByPost(like.getPost());

        final List<Alarm> alarmList = new ArrayList<>();
        for (Tag tag : tagList) {
            final Member taggedMember = tag.getMember();
            if (isSenderEqualToReceiver(like, taggedMember)) continue;
            alarmList.add(
                    new LikeAlarm(
                            like,
                            taggedMember,
                            AlarmType.TAGGED_LIKE
                    )
            );
        }
        alarmRepository.saveAll(alarmList);
    }

    private boolean isSenderEqualToReceiver(
            final LikeEntity like,
            final Member receiver
    ) {
        return like.getMember().equals(receiver);
    }

    public void deleteAlarmByLike(final LikeEntity like) {
        alarmRepository.deleteAlarmByLike(like.getId());
    }
}
