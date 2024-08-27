package com.apps.pochak.fcm.service;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.fcm.dto.FCMToken;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
