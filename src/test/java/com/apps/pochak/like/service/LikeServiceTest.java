package com.apps.pochak.like.service;

import com.apps.pochak.auth.domain.Accessor;
import com.apps.pochak.global.ServiceTest;
import com.apps.pochak.like.domain.LikeEntity;
import com.apps.pochak.like.domain.repository.LikeRepository;
import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import com.apps.pochak.post.domain.Post;
import com.apps.pochak.post.domain.repository.PostRepository;
import com.apps.pochak.post.fixture.PostFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.apps.pochak.global.BaseEntityStatus.ACTIVE;
import static com.apps.pochak.global.BaseEntityStatus.DELETED;
import static com.apps.pochak.member.fixture.MemberFixture.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class LikeServiceTest extends ServiceTest {

    @Autowired
    LikeService likeService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    LikeRepository likeRepository;

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


    @DisplayName("[좋아요] 좋아요가 정상적으로 저장된다.")
    @Test
    void likePost() throws Exception {
        // given
        Post post = savePublicPost();

        // when
        likeService.likePost(
                Accessor.member(loginMember.getId()),
                post.getId()
        );

        // then
        List<LikeEntity> likeList = likeRepository.findAll();
        LikeEntity likeEntity = likeList.stream()
                .filter(like -> like.getMember().equals(loginMember)
                        && like.getPost().getId().equals(post.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("좋아요 저장 실패"));
        assertAll(
                () -> assertEquals(1, likeList.size()),
                () -> assertEquals(ACTIVE, likeEntity.getStatus())
        );
    }

    @DisplayName("[좋아요] 좋아요가 정상적으로 취소된다.")
    @Test
    void cancelLike() throws Exception{
        // given
        Post post = savePublicPost();

        // when
        likeService.likePost(
                Accessor.member(loginMember.getId()),
                post.getId()
        );
        likeService.likePost(
                Accessor.member(loginMember.getId()),
                post.getId()
        );

        // then
        List<LikeEntity> likeList = likeRepository.findAll();
        LikeEntity likeEntity = likeList.stream()
                .filter(like -> like.getMember().equals(loginMember)
                        && like.getPost().getId().equals(post.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("좋아요 저장 실패"));
        assertAll(
                () -> assertEquals(1, likeList.size()),
                () -> assertEquals(DELETED, likeEntity.getStatus())
        );
    }

    private Post savePublicPost() {
        Post post = postRepository.save(PostFixture.get(owner));
        post.makePublic();
        return post;
    }
}
