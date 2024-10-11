package com.apps.pochak.alarm.repository;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.FollowAlarm;
import com.apps.pochak.alarm.domain.LikeAlarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.alarm.fixture.AlarmFixture;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.follow.fixture.FollowFixture;
import com.apps.pochak.like.domain.LikeEntity;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.like.fixture.LikeFixture;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.member.fixture.MemberFixture;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.fixture.PostFixture;
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

    @Autowired
    AlarmRepository alarmRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FollowRepository followRepository;
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private PostRepository postRepository;

    @DisplayName("[알람 조회] 수신자의 팔로우 알람이 조회된다.")
    @Test
    void getFollowAlarmByReceiverId() {
        // given
        Member receiverMember = memberRepository.save(MemberFixture.OWNER);
        Member senderMember = memberRepository.save(MemberFixture.LOGIN_MEMBER);
        Follow follow = followRepository.save(FollowFixture.FOLLOW);

        FollowAlarm followAlarm = alarmRepository.save(AlarmFixture.FOLLOW_ALARM);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Alarm> alarms = alarmRepository.getAllAlarm(receiverMember.getId(), pageable);

        // then
        assertEquals(1, alarms.getTotalElements());
        assertEquals(followAlarm.getId(), alarms.getContent().get(0).getId());
    }

    @DisplayName("[알람 조회] 수신자의 좋아요 알람이 조회된다.")
    @Test
    void getLikeAlarmByReceiverId() {
        // given
        Member receiverMember = memberRepository.save(MemberFixture.LOGIN_MEMBER);
        Member senderMember = memberRepository.save(MemberFixture.OWNER);
        Post post = postRepository.save(PostFixture.PUBLIC_POST);
        LikeEntity likeEntity = likeRepository.save(LikeFixture.LIKE);

        LikeAlarm likeAlarm = alarmRepository.save(AlarmFixture.OWNER_LIKE_ALARM);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Alarm> alarms = alarmRepository.getAllAlarm(receiverMember.getId(), pageable);

        // then
        assertEquals(1, alarms.getTotalElements());
        assertEquals(likeAlarm.getId(), alarms.getContent().get(0).getId());
    }
}
