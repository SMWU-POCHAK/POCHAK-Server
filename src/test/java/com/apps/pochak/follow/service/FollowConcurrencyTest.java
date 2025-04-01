package com.apps.pochak.follow.service;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.block.domain.repository.BlockRepository;
import com.apps.pochak.follow.domain.Follow;
import com.apps.pochak.follow.domain.repository.FollowRepository;
import com.apps.pochak.like.domain.LikeEntity;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.like.service.LikeService;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.fixture.PostFixture;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.apps.pochak.global.BaseEntityStatus.ACTIVE;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class FollowConcurrencyTest {

    @Autowired
    FollowService followService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    BlockRepository blockRepository;

    @Autowired
    EntityManager em;

    private Member sender;
    private Member receiver;

    @BeforeEach
    void setUp() {
        sender = memberRepository.save(TAGGED_MEMBER1);
        receiver = memberRepository.save(TAGGED_MEMBER2);
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
        em.clear();
    }

    @DisplayName("[팔로우] 연속 팔로우 동작이 정상적으로 처리된다.")
    @Test
    void consecutiveFollow() throws InterruptedException {
        // given
        final int threadCount = 3;
        final ExecutorService executorService = Executors.newFixedThreadPool(30);
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    followService.follow(
                            Accessor.member(sender.getId()),
                            receiver.getHandle()
                    );
                } catch (DataIntegrityViolationException e) {
                    e.printStackTrace();
                }
                finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        final Follow follow = followRepository.findBySenderAndReceiver(sender, receiver);

        // then
        assertEquals(ACTIVE, follow.getStatus());
    }
}
