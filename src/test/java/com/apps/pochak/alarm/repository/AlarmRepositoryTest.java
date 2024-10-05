package com.apps.pochak.alarm.repository;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.FollowAlarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.alarm.fixture.AlarmFixture;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.follow.fixture.FollowFixture;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.member.fixture.MemberFixture;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class AlarmRepositoryTest {

    private static final Member SENDER_MEMBER = MemberFixture.STATIC_MEMBER2;
    private static final Member RECEIVER_MEMBER = MemberFixture.STATIC_MEMBER1;
    private static final Follow FOLLOW = FollowFixture.STATIC_RECEIVE_FOLLOW;
    private static final FollowAlarm FOLLOW_ALARM = AlarmFixture.STATIC_FOLLOW_ALARM;

    @Autowired
    AlarmRepository alarmRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    private FollowRepository followRepository;

    private SavedAlarmData saveAlarms() {
        return SavedAlarmData.of()
                .receiver(memberRepository.save(RECEIVER_MEMBER))
                .sender(memberRepository.save(SENDER_MEMBER))
                .follow(followRepository.save(FOLLOW))
                .savedAlarm(alarmRepository.save(FOLLOW_ALARM))
                .build();
    }

    @DisplayName("수신자의 모든 알람이 조회된다.")
    @Test
    void getAllAlarmByReceiverId() {
        // given
        SavedAlarmData savedAlarmData = saveAlarms();
        Member receiver = savedAlarmData.getReceiver();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Alarm> alarms = alarmRepository.getAllAlarm(receiver.getId(), pageable);

        // then
        assertEquals(1, alarms.getTotalElements());
        assertEquals(savedAlarmData.getSavedAlarm().getId(), alarms.getContent().get(0).getId());
    }
}

@Data
class SavedAlarmData {
    private FollowAlarm savedAlarm;
    private Member receiver;
    private Member sender;
    private Follow follow;

    @Builder(builderMethodName = "of")
    public SavedAlarmData(
            final FollowAlarm savedAlarm,
            final Member receiver,
            final Member sender,
            final Follow follow
    ) {
        this.savedAlarm = savedAlarm;
        this.receiver = receiver;
        this.sender = sender;
        this.follow = follow;
    }
}
