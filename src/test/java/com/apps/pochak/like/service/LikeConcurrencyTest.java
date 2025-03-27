package com.apps.pochak.like.service;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.like.domain.LikeEntity;
import com.apps.pochak.like.domain.repository.LikeRepository;
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
import static com.apps.pochak.member.fixture.MemberFixture.LOGIN_MEMBER;
import static com.apps.pochak.member.fixture.MemberFixture.OWNER;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class LikeConcurrencyTest {

    @Autowired
    LikeService likeService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    EntityManager em;

    private Member owner;
    private Member loginMember;

    @BeforeEach
    void setUp() {
        owner = memberRepository.save(OWNER);
        loginMember = memberRepository.save(LOGIN_MEMBER);
    }

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
        postRepository.deleteAll();
        likeRepository.deleteAll();
        em.clear();
    }

    @DisplayName("[좋아요] 연속 좋아요 동작이 정상적으로 처리된다.")
    @Test
    void consecutiveLike() throws InterruptedException {
        // given
        final int threadCount = 3;
        final ExecutorService executorService = Executors.newFixedThreadPool(30);
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        Post post = savePublicPost();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    likeService.likePost(
                            Accessor.member(loginMember.getId()),
                            post.getId()
                    );
                } catch (DataIntegrityViolationException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    likeRepository.deleteAll();
                }
                finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        final LikeEntity like = likeRepository.findByMemberAndPost(loginMember, post).orElseThrow();

        // then
        assertEquals(ACTIVE, like.getStatus());
    }

    private Post savePublicPost() {
        Post post = postRepository.save(PostFixture.get(owner));
        post.makePublic();
        return post;
    }
}
