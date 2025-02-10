package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.follow.service.FollowService;
import com.apps.pochak.global.ServiceTest;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.dto.request.PostUploadRequest;
import com.apps.pochak.post.service.PostService;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import com.apps.pochak.tag.service.TagService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apps.pochak.alarm.domain.AlarmType.MOMENT_POST;
import static com.apps.pochak.global.MockMultipartFileConverter.getMockMultipartFileOfPost;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PostAlarmServiceTest extends ServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    AlarmRepository alarmRepository;

    @Autowired
    PostService postService;

    @Autowired
    TagService tagService;

    @Autowired
    FollowService followService;

    @Autowired
    EntityManager em;

    private Member owner;
    private Member pinnedMember;
    private Member taggedMember;
    private Member loginMember;
    private Member member;

    @Autowired
    private FollowRepository followRepository;

    @BeforeEach
    void setUp() {
        owner = memberRepository.save(OWNER);
        pinnedMember = memberRepository.save(TAGGED_MEMBER1);
        taggedMember = memberRepository.save(TAGGED_MEMBER2);
        loginMember = memberRepository.save(LOGIN_MEMBER);
        member = memberRepository.save(MEMBER);
    }

    @DisplayName("순간포착 게시물이 저장될 경우, 포차커와 포차키의 공통 팔로워에 알림이 전송된다.")
    @Test
    void saveMomentPostAlarm() throws Exception {
        // given
        Post post = savePost();
        follow(loginMember, owner);
        follow(loginMember, pinnedMember);
        follow(member, owner);
        alarmRepository.deleteAll();

        // when
        acceptPost(pinnedMember, post);
        acceptPost(taggedMember, post);

        // then
        List<Alarm> alarmList = alarmRepository.findAll();
        assertAll(
                () -> assertEquals(1, alarmList.size()),
                () -> assertEquals(MOMENT_POST, alarmList.get(0).getAlarmType()),
                () -> assertThat(alarmList)
                        .extracting(Alarm::getReceiver)
                        .containsExactlyInAnyOrder(loginMember)
        );
    }

    private Post savePost() throws Exception {
        PostUploadRequest request = new PostUploadRequest(
                getMockMultipartFileOfPost(),
                "test caption",
                pinnedMember.getHandle(),
                List.of(taggedMember.getHandle())
        );

        postService.savePost(
                Accessor.member(owner.getId()),
                request
        );

        return postRepository.findAll().get(0);
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

    private void acceptPost(
            final Member member,
            final Post post
    ) {
        Tag tag = tagRepository.findTagsByPost(post)
                .stream()
                .filter(t -> t.getMember().equals(member))
                .findFirst()
                .orElseThrow(() -> new AssertionError("게시물 저장 실패"));

        tagService.approveOrRejectTagRequest(
                Accessor.member(member.getId()),
                tag.getId(),
                true
        );
    }
}