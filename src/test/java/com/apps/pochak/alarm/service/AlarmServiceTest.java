package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.AlarmType;
import com.apps.pochak.alarm.domain.CommentAlarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.alarm.dto.response.AlarmElements;
import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.comment.domain.repository.CommentRepository;
import com.apps.pochak.comment.fixture.CommentFixture;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.member.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.data.domain.PageRequest;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("알람 서비스 테스트")
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AlarmServiceTest {

    @Autowired
    AlarmService alarmService;

    @Autowired
    AlarmRepository alarmRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    CommentRepository commentRepository;

    private static final Member OWNER = MemberFixture.OWNER;
    private static final Comment COMMENT = CommentFixture.STATIC_PARENT_COMMENT;

    @BeforeEach
    void setUp() {
        memberRepository.save(OWNER);
        commentRepository.save(COMMENT);
    }

    @DisplayName("[알람 조회] 로그인 사용자의 알람이 정상적으로 조회된다.")
    @Test
    void getAllAlarms() {
        // given
        saveCommentAlarm(COMMENT, OWNER, AlarmType.OWNER_COMMENT);

        Accessor accessor = Accessor.member(OWNER.getId());
        Pageable pageable = PageRequest.of(0, 10);

        AlarmElements expected = new AlarmElements(
                alarmRepository.getAllAlarm(accessor.getMemberId(), pageable)
        );

        // when
        AlarmElements actual = alarmService.getAllAlarms(accessor, pageable);

        // then
        assertThat(actual.getAlarmList())
                .hasSize(expected.getAlarmList().size())
                .containsAll(expected.getAlarmList());

    }

    private void saveCommentAlarm(Comment comment, Member receiver, AlarmType alarmType) {
        CommentAlarm commentAlarm = new CommentAlarm(comment, receiver, alarmType);
        alarmRepository.save(commentAlarm);
    }
}
