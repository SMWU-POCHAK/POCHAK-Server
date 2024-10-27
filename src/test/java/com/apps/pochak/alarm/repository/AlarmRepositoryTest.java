package com.apps.pochak.alarm.repository;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.FollowAlarm;
import com.apps.pochak.alarm.domain.LikeAlarm;
import com.apps.pochak.alarm.domain.TagAlarm;
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
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import com.apps.pochak.tag.fixture.TagFixture;
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
    @Autowired
    private TagRepository tagRepository;

    @DisplayName("[알람 조회] 수신자의 알람들이 조회된다.")
    @Test
    void getAlarmsByReceiverId() {
        // given
        Member loginMember = memberRepository.save(MemberFixture.LOGIN_MEMBER);
        Member postOwner = memberRepository.save(MemberFixture.OWNER);

        Follow follow = followRepository.save(FollowFixture.FOLLOW);
        FollowAlarm followAlarmByPostOwner = alarmRepository.save(AlarmFixture.FOLLOW_ALARM);

        Post post = postRepository.save(PostFixture.PUBLIC_POST);
        LikeEntity likeEntity = likeRepository.save(LikeFixture.LIKE);
        LikeAlarm likeAlarmByLoginMember = alarmRepository.save(AlarmFixture.LIKE_ALARM);

        Tag tag = tagRepository.save(TagFixture.TAG);
        TagAlarm tagAlarmByPostOwner = alarmRepository.save(AlarmFixture.TAG_ALARM);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Alarm> loginMemberAlarms = alarmRepository.getAllAlarm(loginMember.getId(), pageable);

        Page<Alarm> taggedMemberAlarms = alarmRepository.getAllAlarm(postOwner.getId(), pageable);

        // then
        assertEquals(2, loginMemberAlarms.getTotalElements());
        assertEquals(followAlarmByPostOwner.getId(), loginMemberAlarms.getContent().get(0).getId());
        assertEquals(tagAlarmByPostOwner.getId(), loginMemberAlarms.getContent().get(1).getId());

        assertEquals(1, taggedMemberAlarms.getTotalElements());
        assertEquals(likeAlarmByLoginMember.getId(), taggedMemberAlarms.getContent().get(0).getId());
    }

}
