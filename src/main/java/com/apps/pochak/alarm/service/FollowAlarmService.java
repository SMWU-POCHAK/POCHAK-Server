package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.FollowAlarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.fcm.service.FCMService;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowAlarmService {
    private final AlarmRepository alarmRepository;
    private final FCMService fcmService;

    public void sendFollowAlarm(
            final Follow follow,
            final Member receiver
    ) {
        FollowAlarm alarm = new FollowAlarm(follow, receiver);
        alarmRepository.save(alarm);
        fcmService.sendPushNotification(alarm);
    }

    public void deleteFollowAlarm(final Follow follow) {
        alarmRepository.deleteAlarmByFollow(follow.getId());
    }
}
