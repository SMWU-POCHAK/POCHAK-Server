package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.Alarm;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
import com.apps.pochak.alarm.fixture.AlarmFixture;
import com.apps.pochak.comment.domain.Comment;
import com.apps.pochak.comment.domain.repository.CommentRepository;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.like.domain.LikeEntity;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.tag.domain.Tag;
import com.apps.pochak.tag.domain.repository.TagRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.apps.pochak.comment.fixture.CommentFixture.STATIC_CHILD_COMMENT;
import static com.apps.pochak.comment.fixture.CommentFixture.STATIC_PARENT_COMMENT;
import static com.apps.pochak.follow.fixture.FollowFixture.STATIC_RECEIVE_FOLLOW;
import static com.apps.pochak.like.fixture.LikeFixture.STATIC_LIKE2;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static com.apps.pochak.post.fixture.PostFixture.STATIC_PRIVATE_POST;
import static com.apps.pochak.post.fixture.PostFixture.STATIC_PUBLIC_POST;
import static com.apps.pochak.tag.fixture.TagFixture.STATIC_WAITING_TAG;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ExtendWith(SpringExtension.class)
public class AlarmSchedulerTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AlarmDeletionScheduler alarmDeletionScheduler;

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private LikeRepository likeRepository;

    private Member member1;
    private Member member2;
    private Post post1;
    private Post post2;
    private Comment parentComment;
    private Comment childComment;
    private Member loginMember;
    private Follow follow;
    private Tag tag;
    private LikeEntity like;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(STATIC_MEMBER1);
        member2 = memberRepository.save(STATIC_MEMBER2);
        post1 = postRepository.save(STATIC_PUBLIC_POST);
        post2 = postRepository.save(STATIC_PRIVATE_POST);
        parentComment = commentRepository.save(STATIC_PARENT_COMMENT);
        childComment = commentRepository.save(STATIC_CHILD_COMMENT);
        follow = followRepository.save(STATIC_RECEIVE_FOLLOW);
        tag = tagRepository.save(STATIC_WAITING_TAG);
        like = likeRepository.save(STATIC_LIKE2);
    }

    @DisplayName("알람 자동 삭제를 테스트한다.")
    @Test
    void deleteExpiredAlarms() throws Exception {
        // given
        LocalDateTime expiredDate = LocalDateTime.now().plusDays(60)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        Clock clock = Mockito.mock(Clock.class);
        Mockito.when(clock.instant())
                .thenReturn(expiredDate
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toInstant()
                );
        Mockito.when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));
        ReflectionTestUtils.setField(alarmDeletionScheduler, "clock", clock);

        Alarm expiredAlarm = AlarmFixture.STATIC_COMMENT_REPLY_ALARM;
        expiredAlarm.setIsChecked(true);

        Alarm tagAlarm = alarmRepository.save(AlarmFixture.STATIC_TAG_ALARM);

        Alarm uncheckedAlarm = alarmRepository.save(AlarmFixture.STATIC_TAGGED_LIKE_ALARM);


        LocalDateTime notExpiredDate = LocalDateTime.now().plusDays(20)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        Mockito.when(clock.instant())
                .thenReturn(notExpiredDate
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toInstant()
                );
        Mockito.when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));
        ReflectionTestUtils.setField(alarmDeletionScheduler, "clock", clock);

        Alarm recentAlarm = alarmRepository.save(AlarmFixture.STATIC_FOLLOW_ALARM);

        // when
        alarmDeletionScheduler.deleteExpiredAlarms();

        // then
        List<Alarm> remainingAlarms = alarmRepository.findAll();
        assertThat(remainingAlarms).hasSize(3);

        assertThat(remainingAlarms).extracting("id")
                .containsExactlyInAnyOrder(
                        recentAlarm.getId(),
                        tagAlarm.getId(),
                        uncheckedAlarm.getId()
                );

        assertThat(remainingAlarms).doesNotContain(expiredAlarm);
    }

}


