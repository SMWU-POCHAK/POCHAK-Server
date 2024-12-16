package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.alarm.dto.response.AlarmElements;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.ServiceTest;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apps.pochak.global.Constant.DEFAULT_PAGING_SIZE;
import static com.apps.pochak.global.MockMultipartFileConverter.getMockMultipartFileOfPost;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AlarmServiceTest extends ServiceTest {

    @Autowired
    AlarmService alarmService;

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

    @BeforeEach
    void setUp() {
        owner = memberRepository.save(OWNER);
        taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
    }

    @DisplayName("알람이 정상적으로 조회된다.")
    @Test
    void getAllAlarms() throws Exception {
        // given
        savePost();

        AlarmElements expected = new AlarmElements(
                alarmRepository.getAllAlarm(
                        taggedMember1.getId(),
                        PageRequest.of(0, DEFAULT_PAGING_SIZE)
                ));

        // when
        AlarmElements actual = alarmService.getAllAlarms(
                Accessor.member(taggedMember1.getId()),
                PageRequest.of(0, DEFAULT_PAGING_SIZE)
        );

        // then
        assertEquals(expected, actual);
    }

    @DisplayName("알람을 정상적으로 확인한다.")
    @Test
    void checkAlarm() throws Exception {
        // given
        savePost();
        Alarm alarm = alarmRepository.findAll().get(0);
        Member receiver = alarm.getReceiver();

        // when
        alarmService.checkAlarm(
                Accessor.member(receiver.getId()),
                alarm.getId()
        );

        // then
        Alarm checkedAlarm = alarmRepository.findById(alarm.getId()).orElseThrow();
        assertTrue(checkedAlarm.getIsChecked());
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