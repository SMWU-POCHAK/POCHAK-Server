package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.auth.domain.Accessor;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apps.pochak.alarm.domain.AlarmType.TAG_APPROVAL;
import static com.apps.pochak.global.MockMultipartFileConverter.getMockMultipartFileOfPost;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest
class TagAlarmServiceTest extends ServiceTest {

    @Autowired
    PostService postService;

    @Autowired
    TagService tagService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    AlarmRepository alarmRepository;

    private Member owner;
    private Member taggedMember1;
    private Member taggedMember2;
    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    void setUp() {
        owner = memberRepository.save(OWNER);
        taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
    }

    @DisplayName("게시물 수락 알림이 저장된다.")
    @Test
    void saveTagApprovalAlarms() throws Exception {
        // when
        savePost();

        // then
        List<Alarm> alarmList = alarmRepository.findAll();
        assertAll(
                () -> assertEquals(2, alarmList.size()),
                () -> assertThat(alarmList)
                        .extracting(Alarm::getReceiver)
                        .containsExactlyInAnyOrder(taggedMember1, taggedMember2),
                () -> assertThat(alarmList)
                        .extracting(Alarm::getAlarmType)
                        .containsOnly(TAG_APPROVAL)
        );
    }

    @DisplayName("게시물 수락 시 알림이 삭제된다.")
    @Test
    void deleteAlarm_WhenPostAccepted() throws Exception {
        // given
        Post post = savePost();

        // when
        Tag tag = tagRepository.findTagsByPost(post)
                .stream()
                .filter(t -> t.getMember().equals(taggedMember1))
                .findFirst()
                .orElseThrow(() -> new AssertionError("게시물 저장 실패"));

        tagService.approveOrRejectTagRequest(
                Accessor.member(taggedMember1.getId()),
                tag.getId(),
                true
        );

        // then
        List<Alarm> alarmList = alarmRepository.findAll();
        assertAll(
                () -> assertEquals(1, alarmList.size()),
                () -> assertThat(alarmList)
                        .extracting(Alarm::getReceiver)
                        .containsExactlyInAnyOrder(taggedMember2),
                () -> assertThat(alarmList)
                        .extracting(Alarm::getAlarmType)
                        .containsOnly(TAG_APPROVAL)
        );
    }

    @DisplayName("게시물 거절 시 모든 알림이 삭제된다.")
    @Test
    void deleteAlarm_WhenPostRejected() throws Exception {
        // given
        Post post = savePost();

        // when
        Tag tag = tagRepository.findTagsByPost(post)
                .stream()
                .filter(t -> t.getMember().equals(taggedMember1))
                .findFirst()
                .orElseThrow(() -> new AssertionError("게시물 저장 실패"));

        tagService.approveOrRejectTagRequest(
                Accessor.member(taggedMember1.getId()),
                tag.getId(),
                false
        );

        // then
        List<Alarm> alarmList = alarmRepository.findAll();
        assertEquals(0, alarmList.size());
    }


    private Post savePost() throws Exception {
        PostUploadRequest request = new PostUploadRequest(
                getMockMultipartFileOfPost(),
                "test caption",
                List.of(taggedMember1.getHandle(), taggedMember2.getHandle())
        );

        postService.savePost(
                Accessor.member(owner.getId()),
                request
        );

        return postRepository.findAll().get(0);
    }
}