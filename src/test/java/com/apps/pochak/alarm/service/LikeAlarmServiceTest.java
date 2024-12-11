package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.like.service.LikeService;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.dto.request.PostUploadRequest;
import com.apps.pochak.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apps.pochak.alarm.domain.AlarmType.OWNER_LIKE;
import static com.apps.pochak.alarm.domain.AlarmType.TAGGED_LIKE;
import static com.apps.pochak.global.MockMultipartFileConverter.getMockMultipartFileOfPost;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest
class LikeAlarmServiceTest {

    @Autowired
    LikeService likeService;

    @Autowired
    PostService postService;

    @Autowired
    AlarmRepository alarmRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    private Member owner;
    private Member taggedMember1;
    private Member taggedMember2;
    private Member loginMember;

    @BeforeEach
    void setUp() {
        owner = memberRepository.save(OWNER);
        taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
        loginMember = memberRepository.save(LOGIN_MEMBER);
    }

    @DisplayName("좋아요 알림이 성공적으로 저장된다.")
    @Test
    void saveLikeAlarm() throws Exception {
        // given
        Post post = savePublicPost();

        // when
        like(loginMember, post);

        // then
        List<Alarm> alarmList = alarmRepository.findAll();
        Alarm ownerAlarm = alarmList.stream()
                .filter(alarm -> alarm.getReceiver().equals(owner))
                .findFirst()
                .orElseThrow(() -> new AssertionError("알람 저장 실패"));
        List<Alarm> taggedMemberAlarmList = alarmList.stream()
                .filter(alarm -> !alarm.getReceiver().equals(owner))
                .toList();

        assertAll(
                () -> assertEquals(3, alarmList.size()),
                () -> assertEquals(OWNER_LIKE, ownerAlarm.getAlarmType()),
                () -> assertThat(taggedMemberAlarmList)
                        .extracting(Alarm::getReceiver)
                        .containsExactlyInAnyOrder(taggedMember1, taggedMember2),
                () -> assertThat(taggedMemberAlarmList)
                        .extracting(Alarm::getAlarmType)
                        .containsOnly(TAGGED_LIKE)
        );
    }

    @DisplayName("게시물 주인이 좋아요를 눌렀을 경우 알람을 전송하지 않는다.")
    @Test
    void saveLikeAlarm_WhenOwnerLike() throws Exception{
        // given
        Post post = savePublicPost();

        // when
        like(owner, post);

        // then
        List<Alarm> alarmList = alarmRepository.findAll();

        assertAll(
                () -> assertEquals(2, alarmList.size()),
                () -> assertThat(alarmList)
                        .extracting(Alarm::getReceiver)
                        .containsExactlyInAnyOrder(taggedMember1, taggedMember2),
                () -> assertThat(alarmList)
                        .extracting(Alarm::getAlarmType)
                        .containsOnly(TAGGED_LIKE)
        );
    }

    @DisplayName("게시물 주인이 좋아요를 눌렀을 경우 알람을 전송하지 않는다.")
    @Test
    void saveLikeAlarm_WhenTaggedMemberLike() throws Exception{
        // given
        Post post = savePublicPost();

        // when
        like(taggedMember1, post);

        // then
        List<Alarm> alarmList = alarmRepository.findAll();
        Alarm ownerAlarm = alarmList.stream()
                .filter(alarm -> alarm.getReceiver().equals(owner))
                .findFirst()
                .orElseThrow(() -> new AssertionError("알람 저장 실패"));
        List<Alarm> taggedMemberAlarmList = alarmList.stream()
                .filter(alarm -> !alarm.getReceiver().equals(owner))
                .toList();

        assertAll(
                () -> assertEquals(2, alarmList.size()),
                () -> assertEquals(OWNER_LIKE, ownerAlarm.getAlarmType()),
                () -> assertThat(taggedMemberAlarmList)
                        .extracting(Alarm::getReceiver)
                        .containsExactlyInAnyOrder(taggedMember2),
                () -> assertThat(taggedMemberAlarmList)
                        .extracting(Alarm::getAlarmType)
                        .containsOnly(TAGGED_LIKE)
        );
    }

    @DisplayName("좋아요를 취소할 경우, 알람이 삭제된다.")
    @Test
    void deleteLikeAlarm() throws Exception{
        // given
        Post post = savePublicPost();

        // when
        like(loginMember, post);
        like(loginMember, post);

        // then
        List<Alarm> alarmList = alarmRepository.findAll();
        assertEquals(0, alarmList.size());
    }

    private void like(final Member member, final Post post) {
        likeService.likePost(Accessor.member(member.getId()), post.getId());
    }

    private Post savePublicPost() throws Exception {
        PostUploadRequest request = new PostUploadRequest(
                getMockMultipartFileOfPost(),
                "test caption",
                List.of(taggedMember1.getHandle(), taggedMember2.getHandle())
        );

        postService.savePost(
                Accessor.member(owner.getId()),
                request
        );

        Post post = postRepository.findAll().get(0);
        post.makePublic();

        alarmRepository.deleteAll();
        return post;
    }
}