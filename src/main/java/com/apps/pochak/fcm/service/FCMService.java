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
import org.springframework.stereotype.Service;

import java.util.List;

import static com.apps.pochak.fcm.util.MessageUtil.makeMessage;
import static com.apps.pochak.fcm.util.MessageUtil.makeMessages;

@Service
@RequiredArgsConstructor
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
        if (!alarm.getReceiver().hasFcmToken()) return;
        try {
            Message message = makeMessage(alarm);
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            // TODO: 푸시 알림 전송 실패가 Exception까지 터뜨릴 문제인가?
//            throw new FCMMessagingException(FAIL_MESSAGING);
        }
    }

    public void sendPushNotification(final List<Alarm> alarmList) {
        try {
            MulticastMessage multicastMessage = makeMessages(alarmList);
            firebaseMessaging.sendEachForMulticast(multicastMessage);
        } catch (FirebaseMessagingException e) {
//            throw new FCMMessagingException(FAIL_MESSAGING);
        }
    }

    @Transactional
    public void deleteToken(final Accessor accessor) {
        Member member = memberRepository.findMemberById(accessor.getMemberId());
        member.updateFcmToken(null);
    }
}
