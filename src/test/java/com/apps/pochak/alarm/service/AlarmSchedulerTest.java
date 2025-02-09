package com.apps.pochak.alarm.service;

import com.apps.pochak.alarm.domain.*;
import com.apps.pochak.alarm.domain.repository.AlarmRepository;
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
import com.apps.pochak.post.fixture.PostFixture;
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

import static com.apps.pochak.member.fixture.MemberFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ExtendWith(SpringExtension.class)
class AlarmSchedulerTest {

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

    private Member loginMember;
    private Member owner;
    private Member taggedMember1;
    private Member taggedMember2;

    @BeforeEach
    void setUp() {
        loginMember = memberRepository.save(LOGIN_MEMBER);
        owner = memberRepository.save(OWNER);
        taggedMember1 = memberRepository.save(TAGGED_MEMBER1);
        taggedMember2 = memberRepository.save(TAGGED_MEMBER2);
    }

    @DisplayName("알람 자동 삭제를 테스트한다.")
    @Test
    void deleteExpiredAlarms() throws Exception {
        // given
        Alarm expiredAlarm = followAlarm(follow(owner, loginMember), loginMember);
        expiredAlarm.check();
        Alarm tagAlarm = tagAlarm(tag(savePublicPost(), taggedMember1), owner, taggedMember1);
        Alarm uncheckedAlarm = likeAlarm(like(loginMember, savePublicPost()), owner);

        LocalDateTime futureDate = LocalDateTime.now().plusDays(90);
        Clock clock = Mockito.mock(Clock.class);
        Mockito.when(clock.instant())
                .thenReturn(futureDate.atZone(ZoneId.of("Asia/Seoul")).toInstant());
        Mockito.when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));
        ReflectionTestUtils.setField(alarmDeletionScheduler, "clock", clock);

        // when
        alarmDeletionScheduler.deleteExpiredAlarms();

        // then
        List<Alarm> remainingAlarms = alarmRepository.findAll();
        assertThat(remainingAlarms).hasSize(2);

        assertThat(remainingAlarms).extracting("id")
                .containsExactlyInAnyOrder(
                        tagAlarm.getId(),
                        uncheckedAlarm.getId()
                );

        assertThat(remainingAlarms).doesNotContain(expiredAlarm);
    }

    private Follow follow(Member sender, Member receiver) {
        Follow follow = Follow.of()
                .sender(sender)
                .receiver(receiver)
                .build();
        return followRepository.save(follow);
    }

    private FollowAlarm followAlarm(Follow follow, Member receiver){
        FollowAlarm followAlarm = new FollowAlarm(
                follow,
                receiver
        );
        return alarmRepository.save(followAlarm);
    }

    private Post savePublicPost() {
        Post post = postRepository.save(PostFixture.get(owner));
        post.makePublic();
        return post;
    }

    private Tag tag(Post post, Member member){
        Tag tag = Tag.builder()
                .post(post)
                .member(member)
                .build();
        return tagRepository.save(tag);
    }

    private TagAlarm tagAlarm(Tag tag, Member owner, Member receiver){
        TagAlarm tagAlarm = new TagAlarm(
                tag,
                owner,
                receiver
        );
        return alarmRepository.save(tagAlarm);
    }

    private LikeEntity like(Member member, Post post){
        LikeEntity like = LikeEntity.builder()
                .member(member)
                .post(post)
                .build();
        return likeRepository.save(like);
    }

    private LikeAlarm likeAlarm(LikeEntity like, Member receiver){
        LikeAlarm likeAlarm = new LikeAlarm(
                like,
                receiver,
                AlarmType.OWNER_LIKE
        );
        return alarmRepository.save(likeAlarm);
    }



}


