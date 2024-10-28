package com.apps.pochak.alarm.repository;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.FollowAlarm;
import com.apps.pochak.alarm.domain.LikeAlarm;
import com.apps.pochak.alarm.domain.TagAlarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.like.domain.LikeEntity;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.member.fixture.MemberFixture;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static com.apps.pochak.alarm.domain.AlarmType.OWNER_LIKE;
import static com.apps.pochak.post.fixture.PostFixture.CAPTION;
import static com.apps.pochak.post.fixture.PostFixture.POST_IMAGE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest
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

    @AfterEach
    void deleteAll() {
        alarmRepository.deleteAll();
        memberRepository.deleteAll();
        followRepository.deleteAll();
        likeRepository.deleteAll();
        postRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @DisplayName("[알람 조회] 수신자의 알람들이 조회된다.")
    @Test
    void getAlarmsByReceiverId() {
        // given
        Member loginMember = memberRepository.save(MemberFixture.LOGIN_MEMBER);
        Member owner = memberRepository.save(MemberFixture.OWNER);

        Follow follow = followRepository.save(new Follow(owner, loginMember));
        FollowAlarm followAlarmToLoginMember = alarmRepository.save(new FollowAlarm(follow, loginMember));

        Post post = postRepository.save(new Post(owner, POST_IMAGE, CAPTION));

        LikeEntity likeEntity = likeRepository.save(new LikeEntity(loginMember, post));
        LikeAlarm likeAlarmToOwner = alarmRepository.save(new LikeAlarm(likeEntity, owner, OWNER_LIKE));

        Tag tag = tagRepository.save(new Tag(post, loginMember));
        TagAlarm tagAlarmToLoginMember = alarmRepository.save(new TagAlarm(tag, owner, loginMember));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Alarm> loginMemberAlarms = alarmRepository.getAllAlarm(loginMember.getId(), pageable);
        Page<Alarm> taggedMemberAlarms = alarmRepository.getAllAlarm(owner.getId(), pageable);

        // then
        assertAll(
                () -> assertEquals(2, loginMemberAlarms.getTotalElements()),
                () -> assertEquals(tagAlarmToLoginMember.getId(), loginMemberAlarms.getContent().get(0).getId()),
                () -> assertEquals(followAlarmToLoginMember.getId(), loginMemberAlarms.getContent().get(1).getId()),
                () -> assertEquals(1, taggedMemberAlarms.getTotalElements()),
                () -> assertEquals(likeAlarmToOwner.getId(), taggedMemberAlarms.getContent().get(0).getId())
        );
    }
}
