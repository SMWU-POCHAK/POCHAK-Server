package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.follow.service.FollowService;
import com.apps.pochak.global.ServiceTest;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apps.pochak.alarm.domain.AlarmType.FOLLOW;
import static com.apps.pochak.member.fixture.MemberFixture.LOGIN_MEMBER;
import static com.apps.pochak.member.fixture.MemberFixture.MEMBER;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class FollowAlarmServiceTest extends ServiceTest {

    @Autowired
    FollowService followService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    AlarmRepository alarmRepository;

    private Member loginMember;
    private Member member;

    @BeforeEach
    void setUp() {
        loginMember = memberRepository.save(LOGIN_MEMBER);
        member = memberRepository.save(MEMBER);
    }

    @DisplayName("팔로우 알림이 성공적으로 저장된다.")
    @Test
    void saveFollowAlarm() throws Exception {
        // when
        follow(loginMember, member);

        // then
        List<Alarm> alarmList = alarmRepository.findAll();
        assertAll(
                () -> assertEquals(1, alarmList.size()),
                () -> assertEquals(FOLLOW, alarmList.get(0).getAlarmType()),
                () -> assertEquals(member, alarmList.get(0).getReceiver())
        );
    }

    @DisplayName("팔로우를 취소할 경우 알림이 삭제된다.")
    @Test
    void deleteFollowAlarm() throws Exception {
        // when
        follow(loginMember, member);
        follow(loginMember, member);

        // then
        List<Alarm> alarmList = alarmRepository.findAll();
        assertEquals(0, alarmList.size());
    }

    private void follow(
            final Member sender,
            final Member receiver
    ) {
        followService.follow(
                Accessor.member(sender.getId()),
                receiver.getHandle()
        );
    }
}