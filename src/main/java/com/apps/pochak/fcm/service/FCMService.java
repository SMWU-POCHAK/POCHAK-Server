package com.apps.pochak.fcm.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.fcm.dto.FCMToken;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.apps.pochak.fcm.util.MessageUtil.makeMessage;
import static com.apps.pochak.fcm.util.MessageUtil.makeMessages;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMService {
    private final MemberRepository memberRepository;
    private final FirebaseMessaging firebaseMessaging;

    @Transactional
    public void saveToken(
            final Accessor accessor,
            final FCMToken fcmToken
    ) {
        Member member = memberRepository.findMemberById(accessor.getMemberId());
        member.updateFcmToken(fcmToken.getToken());
    }

    public void sendPushNotification(final Alarm alarm) {
        if (checkFCMTokenNotInAlarm(alarm)) return;
        try {
            Message message = makeMessage(alarm);
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            // TODO: 로깅 수정
            log.error(
                    String.format("[FCM Error] Token: %s \n Msg: %s", alarm.getReceiver().getFcmToken(), e.getMessage())
            );
        }
    }

    public void sendPushNotification(final List<Alarm> alarmList) {
        if (checkFCMTokenNotInAlarm(alarmList)) return;
        try {
            MulticastMessage multicastMessage = makeMessages(alarmList);
            firebaseMessaging.sendEachForMulticast(multicastMessage);
        } catch (FirebaseMessagingException e) {
            log.error(String.format("[FCM Error]\n Msg: %s", e.getMessage()));
        }
    }

    private boolean checkFCMTokenNotInAlarm(final Alarm alarm) {
        return !alarm.getReceiver().hasFcmToken();
    }

    private boolean checkFCMTokenNotInAlarm(final List<Alarm> alarmList) {
        return alarmList.stream()
                .noneMatch(alarm -> alarm.getReceiver().hasFcmToken());
    }

    @Transactional
    public void deleteToken(final Accessor accessor) {
        Member member = memberRepository.findMemberById(accessor.getMemberId());
        member.updateFcmToken(null);
    }
}
