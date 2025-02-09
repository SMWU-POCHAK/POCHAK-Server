package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.AlarmType;
import com.apps.pochak.alarm.domain.PostAlarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.fcm.service.FCMService;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.apps.pochak.global.Constant.DEFAULT_DELETION_SIZE;
import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;

@Service
@RequiredArgsConstructor
public class PostAlarmService {
    private final AlarmRepository alarmRepository;
    private final FollowRepository followRepository;
    private final FCMService fcmService;

    @Async
    public void saveMomentPostAlarm(final Post post) {
        PageRequest pageRequest = PageRequest.of(0, DEFAULT_DELETION_SIZE);
        Page<Follow> commonFollowers;
        do {
            commonFollowers = followRepository.findCommonFollowers(
                    post.getOwner(),
                    post.getPinnedMember(),
                    PageRequest.of(0, DEFAULT_PAGING_SIZE)
            );
            List<PostAlarm> alarmList = commonFollowers.getContent().stream().map(
                    f -> new PostAlarm(post, f.getSender(), AlarmType.MOMENT_POST)
            ).toList();
            alarmRepository.saveAll(alarmList);
            fcmService.sendPushNotification((Alarm) alarmList);
            pageRequest = pageRequest.next();
        } while (commonFollowers.hasNext());
    }
}
